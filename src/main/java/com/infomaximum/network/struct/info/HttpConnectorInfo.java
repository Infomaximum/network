package com.infomaximum.network.struct.info;

public class HttpConnectorInfo {

    protected final String host;
    protected final int port;

    public HttpConnectorInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "HttpConnector{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
