package com.infomaximum.network.transport.socket.builder;

import com.infomaximum.network.builder.BuilderTransport;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by kris on 29.08.16.
 */
public class SocketBuilderTransport extends BuilderTransport {

    private InetAddress host;
    private int port;

    public SocketBuilderTransport(int port) {
        this.host=new InetSocketAddress(0).getAddress();
        this.port=port;
    }

    public SocketBuilderTransport withHost(InetAddress host){
        this.host=host;
        return this;
    }

    public InetAddress getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }

}
