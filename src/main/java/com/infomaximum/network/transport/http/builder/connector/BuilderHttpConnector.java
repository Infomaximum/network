package com.infomaximum.network.transport.http.builder.connector;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.struct.info.HttpConnectorInfo;
import org.eclipse.jetty.server.*;

import java.util.function.Supplier;

public class BuilderHttpConnector {

    protected String host;
    protected int port;

    public BuilderHttpConnector(int port) {
        this.port = port;
        this.host = null;
    }

    public BuilderHttpConnector withHost(String host){
        this.host = host;
        return this;
    }


    public Connector build(Server server) throws NetworkException {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setHost(host);
        return connector;
    }

    public Supplier<? extends HttpConnectorInfo> getInfoSupplier() {
        return () -> new HttpConnectorInfo(host, port);
    }
}
