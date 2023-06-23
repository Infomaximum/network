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
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

public class HttpTransport extends Transport<Session> {

    private final static Logger log = LoggerFactory.getLogger(HttpTransport.class);

    public static HttpTransport instance;

    private final Server server;
    private final List<Supplier<? extends HttpConnectorInfo>> connectorInfoSuppliers;

    public HttpTransport(final HttpBuilderTransport httpBuilderTransport) throws NetworkException {
        server = new Server(new QueuedThreadPool(10000));
        connectorInfoSuppliers = new ArrayList<>();

        if (httpBuilderTransport.getBuilderConnectors() == null || httpBuilderTransport.getBuilderConnectors().isEmpty()) {
            throw new NetworkException("Not found connectors");
        }
        List<Connector> connectors = new ArrayList<>();
        for (BuilderHttpConnector builderHttpConnector : httpBuilderTransport.getBuilderConnectors()) {
            connectorInfoSuppliers.add(builderHttpConnector.getInfoSupplier());
            Connector connector = builderHttpConnector.build(server);
            connectors.add(connector);

            //Возможно есть подписчики
            if (httpBuilderTransport.getHttpChannelListeners() != null) {
                for (HttpChannel.Listener listener : httpBuilderTransport.getHttpChannelListeners()) {
                    connector.addBean(listener);
                }
            }
        }
        server.setConnectors(connectors.toArray(new Connector[connectors.size()]));

        ServletContextHandler servletContext = new ServletContextHandler();
        servletContext.setContextPath("/");

        servletContext.addServlet(new ServletHolder(WebSocketServletConfiguration.class), "/ws");
        servletContext.addServlet(httpBuilderTransport.getServletHolder(), "/");

        //Возможно есть регистрируемые фильтры
        if (httpBuilderTransport.getFilters() != null) {
            for (BuilderFilter builderFilter : httpBuilderTransport.getFilters()) {
                servletContext.addFilter(builderFilter.filterClass, builderFilter.pathSpec, builderFilter.dispatches);
            }
        }

        //Инициализирум контекс с вебсокетами
        JettyWebSocketServletContainerInitializer.configure(servletContext, null);

        if (httpBuilderTransport.getCORS() != null) {
            FilterHolder cors = servletContext.addFilter(CrossOriginFilter.class,"/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));
            cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, httpBuilderTransport.getCORS());
            cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,OPTIONS");
            cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Authorization,Content-Type,Accept,Origin,User-Agent,DNT,Cache-Control,X-Mx-ReqToken,Access-Control-Allow-Origin");
        }

        Handler context = servletContext;
        //Добавляем хедлер упаковки ресурсов контекста
        if (httpBuilderTransport.getCompressResponseMimeTypes() != null) {
            GzipHandler gzipHandlerContext = new GzipHandler();
            gzipHandlerContext.setIncludedMimeTypes(httpBuilderTransport.getCompressResponseMimeTypes().toArray(String[]::new));
            gzipHandlerContext.setMinGzipSize(1024);
            gzipHandlerContext.setHandler(servletContext);
            context = gzipHandlerContext;
        }

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{ context, new DefaultHandler() });
        server.setHandler(handlers);

        if (httpBuilderTransport.getErrorHandler() != null) {
            server.setErrorHandler(httpBuilderTransport.getErrorHandler());//jetty 12 migration to: server.setErrorProcessor(httpBuilderTransport.getErrorProcessor());
        }

        try {
            server.start();
        } catch (Exception e) {
            throw new NetworkException(e);
        }

        instance = this;
    }

    @Override
    public TypeTransport getType() {
        return TypeTransport.HTTP;
    }

    @Override
    public void send(Session session, IPacket packet) throws IOException {
        session.getRemote().sendString(packet.serialize());
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
