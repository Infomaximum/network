package com.infomaximum.network.session;

import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.SessionDataBuilder;
import com.infomaximum.network.struct.SessionData;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * Created by Kris on 29.07.2015.
 */
public class Session {

    private final NetworkImpl network;
    private final TransportSession transportSession;

    public final String uuid;

    private Serializable auth;
    private SessionData sessionData;

    protected Session(NetworkImpl network, TransportSession transportSession) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.network=network;
        this.transportSession=transportSession;

        this.uuid = UUID.randomUUID().toString();

        SessionDataBuilder sessionDataBuilder = network.getSessionDataBuilder();
        if (sessionDataBuilder != null) {
            sessionData = sessionDataBuilder.build();
        }
    }

    public <T extends Serializable> T getAuth() {
        return (T) auth;
    }

    public boolean isLogin() {
        return (auth!=null);
    }

    public <T extends Serializable> void login(T auth) {
        if (this.auth != null) throw new RuntimeException("Repeated auth");
        this.auth = auth;

        network.onLogin(this, auth);
    }

    public void logout(){
        if (this.auth == null) throw new RuntimeException("Not auth");
        this.auth = null;
        network.onLogout(this, auth);
    }

    public SessionData getSessionData() {
        return sessionData;
    }

    public TransportSession getTransportSession() {
        return transportSession;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Session(");
        str.append("uuid='").append(uuid).append('\'');
        if (isLogin()) str.append(", auth='").append(auth).append('\'');
        str.append(')');
        return str.toString();
    }
}
