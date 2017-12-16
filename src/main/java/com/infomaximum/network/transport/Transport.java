package com.infomaximum.network.transport;

import com.infomaximum.network.packet.Packet;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;

/**
 * Created by kris on 26.08.16.
 */
public abstract class Transport<Channel> {

    private final Set<TransportListener> listeners;

    public Transport() {
        this.listeners = new CopyOnWriteArraySet<TransportListener>();
    }

    public void addTransportListener(TransportListener listener) {
        this.listeners.add(listener);
    }

    public void removeTransportListener(TransportListener listener) {
        this.listeners.remove(listener);
    }

    public abstract TypeTransport getType();

    public void fireConnect(Channel channel, String remoteIpAddress) {
        for (TransportListener transportListener: this.listeners) {
            transportListener.onConnect(this, channel, remoteIpAddress);
        }
    }

    public void fireIncomingPacket(Channel channel, Packet packet) {
        for (TransportListener transportListener: this.listeners) {
            transportListener.incomingPacket(this, channel, packet);
        }
    }

    public void fireDisconnect(Channel channel, int statusCode, Throwable throwable) {
        for (TransportListener transportListener: this.listeners) {
            transportListener.onDisconnect(this, channel, statusCode, throwable);
        }
    }

    public abstract Future<Void> send(Channel channel, Packet packet) throws IOException;

    public abstract void destroy() throws Exception;

    public abstract void close(Channel channel) throws IOException;
}
