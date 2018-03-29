package com.infomaximum.network.transport.http.builder.connector;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public class BuilderHttpConnector {

    private String host;
    private int port;

    public BuilderHttpConnector(int port) {
        this.port = port;
        this.host = null;
    }

    public BuilderHttpConnector withHost(String host){
        this.host = host;
        return this;
    }


    public Connector build(Server server) {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setHost(host);
        return connector;
    }
}
