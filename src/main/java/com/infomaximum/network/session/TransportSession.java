package com.infomaximum.network.session;

import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.Protocol;
import com.infomaximum.network.struct.HandshakeData;
import com.infomaximum.network.transport.Transport;
import net.minidev.json.JSONObject;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.common.WebSocketSession;

import java.io.IOException;

public abstract class TransportSession {

    public class RemoteAddress {
        private String rawRemoteAddress;
        private String endRemoteAddress;

        private RemoteAddress(String rawRemoteAddress, String endRemoteAddress) {
            this.rawRemoteAddress = rawRemoteAddress;
            this.endRemoteAddress = endRemoteAddress;
        }

        public String getRawRemoteAddress() {
            return rawRemoteAddress;
        }

        public String getEndRemoteAddress() {
            return endRemoteAddress;
        }
    }

    protected final Protocol protocol;
    protected final Transport transport;
    protected final Object channel;

    public TransportSession(Protocol protocol, Transport transport, Object channel) {
        this.protocol = protocol;
        this.transport = transport;
        this.channel = channel;
    }

    public abstract void completedPhaseHandshake(HandshakeData handshakeData);

    public abstract void failPhaseHandshake(IPacket responsePacket);

    public abstract Session getSession();

    public UpgradeRequest getUpgradeRequest() {
        return ((WebSocketSession) channel).getUpgradeRequest();
    }

    /**
     * Сюда приходят входящие пакеты из сети
     */
    public abstract void incomingPacket(JSONObject jPacket);

    public void send(IPacket packet) throws IOException {
        transport.send(channel, packet);
    }

    public void destroyed() {
        try {
            transport.close(channel);
        } catch (Throwable ignore) {
        }
    }

    public RemoteAddress getRemoteAddress() {
        String endRemoteAddress = ((WebSocketSession) channel).getUpgradeRequest().getHeader("X-Real-IP");
        String rawRemoteAddress = ((WebSocketSession) channel).getRemoteAddress().getAddress().toString().split("/")[1];
        if (endRemoteAddress == null) {
            endRemoteAddress = rawRemoteAddress;
        }
        return new RemoteAddress(rawRemoteAddress, endRemoteAddress);
    }
}
