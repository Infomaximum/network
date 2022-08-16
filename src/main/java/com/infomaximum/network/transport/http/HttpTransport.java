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
import com.infomaximum.network.transport.http.jsp.JspStarter;
import jakarta.servlet.Servlet;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.springframework.context.annotation.AnnotationConfigRegistry;
import jakarta.servlet.http.HttpServlet;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.EnvironmentCapable;


/**
 * Created with IntelliJ IDEA.
 * User: Admin
 * Date: 11.09.13
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
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
        for (BuilderHttpConnector builderHttpConnector: httpBuilderTransport.getBuilderConnectors()) {
            connectorInfoSuppliers.add(builderHttpConnector.getInfoSupplier());
            Connector connector = builderHttpConnector.build(server);
            connectors.add(connector);

            //Возможно есть подписчики
            if (httpBuilderTransport.getHttpChannelListeners()!=null) {
                for (HttpChannel.Listener listener: httpBuilderTransport.getHttpChannelListeners()) {
                    connector.addBean(listener);
                }
            }
        }
        server.setConnectors(connectors.toArray(new Connector[connectors.size()]));

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        context.addServlet(new ServletHolder(WebSocketServletConfiguration.class), "/ws");

        //TODO раскоментировать!!!
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(httpBuilderTransport.getClassWebMvcConfig());
        context.addServlet(new ServletHolder("default", new DispatcherServlet(applicationContext)), "/");

        if (httpBuilderTransport.isSupportJsp()) {
            //Устанавливаем каталог для сборки jsp файлов
            Path scratchDirectory;
            try {
                scratchDirectory = Files.createTempDirectory(null);
            } catch (IOException e) {
                throw new NetworkException(e);
            }
            scratchDirectory.toFile().deleteOnExit();
            context.setAttribute("javax.servlet.context.tempdir", scratchDirectory.toFile());

            context.addBean(new JspStarter(context));

            context.setClassLoader(Thread.currentThread().getContextClassLoader());
            //TODO раскоментировать!!!
            context.addServlet(new ServletHolder("jspServlet", new JspServlet()), "*.jsp");

            //Утснавливаем resourceBase
            URL urlResourceBase = this.getClass().getClassLoader().getResource(httpBuilderTransport.getJspPath());
            if (urlResourceBase == null) throw new RuntimeException("Failed to find path: " + httpBuilderTransport.getJspPath());
            context.setResourceBase( urlResourceBase.toExternalForm() );
        }

        //Возможно есть регистрируемые фильтры
        if (httpBuilderTransport.getFilters()!=null) {
            for (BuilderFilter builderFilter: httpBuilderTransport.getFilters()) {
                context.addFilter(builderFilter.filterClass, builderFilter.pathSpec, builderFilter.dispatches);
            }
        }

        //Инициализирум контекс с вебсокетами
        JettyWebSocketServletContainerInitializer.configure(context, null);

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{ context, new DefaultHandler() });
        server.setHandler(handlers);

        if (httpBuilderTransport.getErrorHandler()!=null) server.setErrorHandler(httpBuilderTransport.getErrorHandler());

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
