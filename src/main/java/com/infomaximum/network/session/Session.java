package com.infomaximum.network.session;

import com.infomaximum.network.struct.HandshakeData;
import com.infomaximum.network.struct.SessionData;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * Created by Kris on 29.07.2015.
 */
public class Session {

    private final TransportSession transportSession;

    public final String uuid;

    private HandshakeData handshakeData;
    private SessionData data;

    public Session(TransportSession transportSession) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.transportSession = transportSession;

        this.uuid = UUID.randomUUID().toString();
    }

    protected void initHandshakeData(HandshakeData handshakeData) {
        if (this.handshakeData != null) {
            throw new RuntimeException("HandshakeData is initialized");
        }
        this.handshakeData = handshakeData;
    }

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
}
