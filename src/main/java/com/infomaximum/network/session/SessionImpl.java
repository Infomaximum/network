package com.infomaximum.network.session;

import com.infomaximum.network.struct.HandshakeData;
import com.infomaximum.network.struct.SessionData;

import java.util.Objects;
import java.util.UUID;

public class SessionImpl implements Session {

    public final String uuid;
    private final TransportSession transportSession;
    private HandshakeData handshakeData;
    private SessionData data;

    public SessionImpl(TransportSession transportSession) {
        this.transportSession = transportSession;

        this.uuid = UUID.randomUUID().toString();
    }

    public void initHandshakeData(HandshakeData handshakeData) {
        if (this.handshakeData != null) {
            throw new RuntimeException("HandshakeData is initialized");
        }
        this.handshakeData = handshakeData;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public HandshakeData getHandshakeData() {
        return handshakeData;
    }

    public TransportSession getTransportSession() {
        return transportSession;
    }

    public void initData(SessionData data) {
        if (this.data != null) {
            throw new RuntimeException("Session data is initialized");
        }
        this.data = data;
    }

    @Override
    public SessionData getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Session(");
        str.append("uuid='").append(uuid).append('\'');
        str.append(')');
        return str.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionImpl session = (SessionImpl) o;
        return uuid.equals(session.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
