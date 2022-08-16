module com.infomaximum.network {
    requires org.slf4j;
    requires net.minidev.jsonsmart;
    requires org.reflections.reflections;
//    requires org.mortbay.jasper.apachejsp;
//    requires org.springframework.springweb;
//    requires org.springframework.springwebmvc;
    requires org.eclipse.jetty.websocket.jetty.api;
    requires org.eclipse.jetty.websocket.jetty.common;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires org.eclipse.jetty.websocket.servlet;
    requires org.eclipse.jetty.websocket.jetty.server;
    requires org.eclipse.jetty.apache.jsp;
    requires org.mortbay.jasper.apachejsp;
    requires spring.web;
//    requires org.springframework.springcontext;
//    requires org.springframework.springcore;


    exports com.infomaximum.network.protocol.standard.session;
    exports com.infomaximum.network.mvc;
    exports com.infomaximum.network.protocol.standard.packet;
    exports com.infomaximum.network.struct;
    exports com.infomaximum.network;
    exports com.infomaximum.network.builder;
    exports com.infomaximum.network.exception;
    exports com.infomaximum.network.transport.http.builder;
    exports com.infomaximum.network.packet;
    exports com.infomaximum.network.session;
    exports com.infomaximum.network.protocol;
    exports com.infomaximum.network.transport;
    exports com.infomaximum.network.transport.http.builder.connector;
    exports com.infomaximum.network.transport.http.builder.filter;
    exports com.infomaximum.network.mvc.anotation;
    exports com.infomaximum.network.protocol.standard.handler.handshake;

    /**
     requires org.slf4j;
     requires net.minidev.jsonsmart;
     requires org.eclipse.jetty.jettyserver;
     requires org.eclipse.jetty.jettyutil;
     requires org.eclipse.jetty.websocket.websocketapi;
     requires org.reflections.reflections;
     requires org.mortbay.jasper.apachejsp;
     requires javax.servlet.javax.servletapi;
     requires org.eclipse.jetty.websocket.websocketserver;
     requires org.eclipse.jetty.websocket.websocketservlet;
     requires org.eclipse.jetty.apachejsp;
     requires org.eclipse.jetty.jettyservlet;
     requires org.springframework.springweb;
     requires org.springframework.springwebmvc;
     requires org.eclipse.jetty.websocket.websocketcommon;
     requires org.eclipse.jetty.jettyio;

     exports com.infomaximum.network.protocol.standard.session;
     exports com.infomaximum.network.mvc;
     exports com.infomaximum.network.protocol.standard.packet;
     exports com.infomaximum.network.struct;
     exports com.infomaximum.network;
     exports com.infomaximum.network.builder;
     exports com.infomaximum.network.exception;
     exports com.infomaximum.network.transport.http.builder;
     exports com.infomaximum.network.packet;
     exports com.infomaximum.network.session;
     exports com.infomaximum.network.protocol;
     exports com.infomaximum.network.transport;
     exports com.infomaximum.network.transport.http.builder.connector;
     exports com.infomaximum.network.transport.http.builder.filter;
     exports com.infomaximum.network.mvc.anotation;
     exports com.infomaximum.network.protocol.standard.handler.handshake;
     */
}