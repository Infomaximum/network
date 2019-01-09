package com.infomaximum.network.transport.http;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.packet.Packet;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.TypeTransport;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.builder.filter.BuilderFilter;
import com.infomaximum.network.transport.http.jsp.JspStarter;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

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

    public HttpTransport(final HttpBuilderTransport httpBuilderTransport) throws NetworkException {
        server = new Server(new QueuedThreadPool(10000));

        if (httpBuilderTransport.getBuilderConnectors() == null || httpBuilderTransport.getBuilderConnectors().isEmpty()) {
            throw new NetworkException("Not found connectors");
        }
        List<Connector> connectors = new ArrayList<>();
        for (BuilderHttpConnector builderHttpConnector: httpBuilderTransport.getBuilderConnectors()) {
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
    public Future<Void> send(Session session, Packet packet) throws IOException {
        return session.getRemote().sendStringByFuture(packet.serialize());
    }

    @Override
    public void close(Session session) throws IOException {
        session.close();
    }

    @Override
    public void destroy() throws Exception {
        server.stop();
        server.destroy();
    }
}
