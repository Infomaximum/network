package com.infomaximum.network.transport.http.builder.connector;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.struct.info.HttpConnectorInfo;
import org.eclipse.jetty.server.*;

import java.util.function.Supplier;

public class BuilderHttpConnector {

    protected String host;
    protected int port;
    protected Integer requestHeaderSize;
    protected Integer responseHeaderSize;

    public BuilderHttpConnector(int port) {
        this.port = port;
        this.host = null;
    }

    public BuilderHttpConnector withHost(String host){
        this.host = host;
        return this;
    }

    public BuilderHttpConnector withRequestHeaderSize(int size){
        this.requestHeaderSize = size;
        return this;
    }

    public BuilderHttpConnector withResponseHeaderSize(int size){
        this.responseHeaderSize = size;
        return this;
    }

    public Connector build(Server server) throws NetworkException {
        ServerConnector connector;
        if (requestHeaderSize != null || responseHeaderSize != null) {
            HttpConfiguration httpsConfig = new HttpConfiguration();
            if (requestHeaderSize != null) {
                httpsConfig.setRequestHeaderSize(requestHeaderSize);
            }
            if (responseHeaderSize != null) {
                httpsConfig.setResponseHeaderSize(responseHeaderSize);
            }
            connector = new ServerConnector(
                    server,
                    new HttpConnectionFactory(httpsConfig));
            connector.setPort( port );
            connector.setHost(host);
        } else {
            connector = new ServerConnector(server);
        }
        connector.setPort(port);
        connector.setHost(host);
        return connector;
    }

    public Supplier<? extends HttpConnectorInfo> getInfoSupplier() {
        return () -> new HttpConnectorInfo(host, port);
    }
}
