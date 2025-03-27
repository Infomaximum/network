package com.infomaximum.network.transport.http;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.struct.info.HttpConnectorInfo;
import com.infomaximum.network.struct.info.TransportInfo;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.TypeTransport;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.builder.filter.BuilderFilter;
import com.infomaximum.network.transport.http.handler.HandlerListeners;
import com.infomaximum.network.transport.http.handler.HttpErrorHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.CrossOriginHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class HttpTransport extends Transport<Session> {

    private final static Logger log = LoggerFactory.getLogger(HttpTransport.class);

    private final Server server;
    private final List<Supplier<? extends HttpConnectorInfo>> connectorInfoSuppliers;

    public HttpTransport(final HttpBuilderTransport httpBuilderTransport, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) throws NetworkException {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setVirtualThreadsExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server = new Server(threadPool);
        connectorInfoSuppliers = new ArrayList<>();

        if (httpBuilderTransport.getBuilderConnectors() == null || httpBuilderTransport.getBuilderConnectors().isEmpty()) {
            throw new NetworkException("Not found connectors");
        }
        List<Connector> connectors = new ArrayList<>();
        for (BuilderHttpConnector builderHttpConnector : httpBuilderTransport.getBuilderConnectors()) {
            connectorInfoSuppliers.add(builderHttpConnector.getInfoSupplier());
            Connector connector = builderHttpConnector.build(server);
            connectors.add(connector);
        }
        server.setConnectors(connectors.toArray(new Connector[connectors.size()]));

        ServletContextHandler servletContext = new ServletContextHandler();
        servletContext.setContextPath("/");

        servletContext.addServlet(new ServletHolder(new WebSocketServletConfiguration(this)), "/ws");
        servletContext.addServlet(httpBuilderTransport.getServletHolder(), "/");

        //Возможно есть регистрируемые фильтры
        if (httpBuilderTransport.getFilters() != null) {
            for (BuilderFilter builderFilter : httpBuilderTransport.getFilters()) {
                if (builderFilter.isInstance()) {
                    servletContext.addFilter(builderFilter.filter, builderFilter.pathSpec, builderFilter.dispatches);
                } else {
                    servletContext.addFilter(builderFilter.filterClass, builderFilter.pathSpec, builderFilter.dispatches);
                }
            }
        }

        //Инициализирум контекс с вебсокетами
        JettyWebSocketServletContainerInitializer.configure(servletContext, null);

        Handler context = servletContext;

        if (httpBuilderTransport.getCORS() != null) {
            CrossOriginHandler crossOriginHandler = new CrossOriginHandler();

            crossOriginHandler.setAllowedOriginPatterns(Set.of(httpBuilderTransport.getCORS()));
            crossOriginHandler.setAllowedMethods(Set.of("GET", "POST", "OPTIONS"));
            crossOriginHandler.setAllowedHeaders(Set.of("Authorization","Content-Type","Accept","Origin","User-Agent","DNT","Cache-Control","X-Mx-ReqToken","Access-Control-Allow-Origin", "X-Request-Id", "X-Trace-Id"));

            crossOriginHandler.setHandler(context);
            context = crossOriginHandler;
        }

        //Добавляем хендлер упаковки ресурсов контекста
        if (httpBuilderTransport.getCompressResponseMimeTypes() != null) {
            GzipHandler gzipHandlerContext = new GzipHandler();
            gzipHandlerContext.setIncludedMimeTypes(httpBuilderTransport.getCompressResponseMimeTypes().toArray(String[]::new));
            gzipHandlerContext.setMinGzipSize(1024);
            gzipHandlerContext.setHandler(context);
            context = gzipHandlerContext;
        }

        //Добавляем хендлер - для подписчиков
        if (httpBuilderTransport.getHttpChannelListeners() != null) {
            context = new HandlerListeners(
                    httpBuilderTransport.getHttpChannelListeners(), uncaughtExceptionHandler, context
            );
        }

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{ context, new DefaultHandler() });
        server.setHandler(handlers);

        if (httpBuilderTransport.getErrorHandler() != null) {
            HttpErrorHandler httpErrorHandler = new HttpErrorHandler(httpBuilderTransport.getErrorHandler(), uncaughtExceptionHandler);
            server.setErrorHandler(httpErrorHandler);
        }

        try {
            server.start();
        } catch (Exception e) {
            throw new NetworkException(e);
        }
    }

    @Override
    public TypeTransport getType() {
        return TypeTransport.HTTP;
    }

    @Override
    public void send(Session session, IPacket packet) throws IOException {
        session.sendText(packet.serialize(), Callback.NOOP);//TODO !!!! стоит обратть внимание - в 12 версии появился колбек надо на него реагировать - кидать ошибку и т.п.
    }

    @Override
    public void close(Session session) throws IOException {
        session.close();
    }

    @Override
    public TransportInfo getInfo() {
        TransportInfo transportInfo = new TransportInfo();
        connectorInfoSuppliers.forEach(infoSupplier -> transportInfo.addConnectorInfo(infoSupplier.get()));
        return transportInfo;
    }

    @Override
    public void destroy() throws Exception {
        server.stop();
        server.destroy();
    }
}
