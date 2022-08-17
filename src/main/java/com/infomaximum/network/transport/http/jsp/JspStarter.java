package com.infomaximum.network.transport.http.jsp;

//import org.apache.tomcat.util.scan.StandardJarScanner;
//import org.apache.tomcat.util.scan.StandardJarScanner;
//import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.listener.ContainerInitializer;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
//import org.apache.jasper.servlet.JasperInitializer;

import java.util.ArrayList;
import java.util.List;

/**
 * JspStarter for embedded ServletContextHandlers
 *
 * This is added as a bean that is a jetty LifeCycle on the ServletContextHandler.
 * This bean's doStart method will be called as the ServletContextHandler starts,
 * and will call the ServletContainerInitializer for the jsp engine.
 *
 */
public class JspStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller {
//    JettyJasperInitializer sci;
    ServletContextHandler context;

    public JspStarter (ServletContextHandler context) {
//        this.sci = new JettyJasperInitializer();
        this.context = context;
//        this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
    }

    @Override
    protected void doStart() throws Exception {
        //TODO для работ jsp - надо поправить(сломалось после миграции на jdk17)
        throw new RuntimeException("No migration to jdk17");

        /*
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(context.getClassLoader());
        try {
            sci.onStartup(null, context.getServletContext());
            super.doStart();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
         */
    }
}