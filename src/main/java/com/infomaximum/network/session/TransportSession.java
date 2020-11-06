package com.infomaximum.network.session;

import com.infomaximum.network.executerequest.ExecuteRequest;
import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.Protocol;
import com.infomaximum.network.struct.HandshakeData;
import com.infomaximum.network.struct.RemoteAddress;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.TransportPacketHandler;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class TransportSession implements TransportPacketHandler {

    private final static Logger log = LoggerFactory.getLogger(TransportSession.class);

    protected final Protocol protocol;
    protected final Transport transport;
    protected final Object channel;
    protected final Session session;
    private final ExecuteRequest requestQueue;

    public TransportSession(Protocol protocol, Transport transport, Object channel) {
        this.protocol = protocol;
        this.transport = transport;
        this.channel = channel;
        this.requestQueue = new ExecuteRequest(this, protocol.uncaughtExceptionHandler);

        this.session = new Session(this);
    }

    public abstract void completedPhaseHandshake(HandshakeData handshakeData);

    public abstract void failPhaseHandshake(IPacket responsePacket);

    @Override
    public Session getSession() {
        return session;
    }

    public UpgradeRequest getUpgradeRequest() {
        return ((WebSocketSession) channel).getUpgradeRequest();
    }

    public void incomingMessage(String message) {
        requestQueue.incomingPacket(message);
    }

    @Override
    public void send(IPacket packet) {
        try {
            transport.send(channel, packet);
        } catch (Throwable e) {
            if (!(e instanceof IOException)) {
                log.error("Exception", e);
            }
            try {
                transport.close(channel);
            } catch (Throwable ignore) {
            }
            destroyed();
        }
    }

    public void destroyed() {
        try {
            transport.close(channel);
        } catch (Throwable ignore) {
        }
    }

    public RemoteAddress buildRemoteAddress() {
        String endRemoteAddress = ((WebSocketSession) channel).getUpgradeRequest().getHeader("X-Real-IP");
        String rawRemoteAddress = ((WebSocketSession) channel).getRemoteAddress().getAddress().toString().split("/")[1];
        if (endRemoteAddress == null) {
            endRemoteAddress = rawRemoteAddress;
        }
        return new RemoteAddress(rawRemoteAddress, endRemoteAddress);
    }
}
