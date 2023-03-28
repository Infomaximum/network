module com.infomaximum.network {
    requires org.slf4j;
    requires net.minidev.jsonsmart;
    requires org.reflections;
    requires org.eclipse.jetty.websocket.jetty.api;
    requires org.eclipse.jetty.websocket.jetty.common;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires org.eclipse.jetty.websocket.servlet;
    requires org.eclipse.jetty.websocket.jetty.server;
    requires spring.web;
    requires spring.webmvc;
    requires spring.context;
    requires spring.core;
    requires org.eclipse.jetty.servlets;

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
    exports com.infomaximum.network.protocol.standard;
    exports com.infomaximum.network.mvc.builder;

    exports com.infomaximum.network.transport.http;
    exports com.infomaximum.network.event;
}