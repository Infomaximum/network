package com.infomaximum.network.session;

import com.infomaximum.network.Network;
import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.struct.ISessionData;

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

    private Serializable user;
    private ISessionData sessionData;

    protected Session(NetworkImpl network, TransportSession transportSession, Class<? extends ISessionData> sessionDataClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.network=network;
        this.transportSession=transportSession;

        this.uuid = UUID.randomUUID().toString();
        if (sessionDataClass!=null) {
            sessionData=sessionDataClass.getConstructor().newInstance();
        }
    }

    public Serializable getUser() {
        return user;
    }

    public boolean isLogin() {
        return (user!=null);
    }

    public void login(Serializable user) {
        if (this.user!=null) throw new RuntimeException("Repeated auth");
        this.user=user;

        network.onLogin(this, user);
    }

    public void logout(){
        if (this.user==null) throw new RuntimeException("Not auth");
        this.user = null;
        network.onLogout(this, user);
    }

    public ISessionData getSessionData() {
        return sessionData;
    }

    public TransportSession getTransportSession() {
        return transportSession;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Session(");
        str.append("uuid='").append(uuid).append('\'');
        if (isLogin()) str.append(", user='").append(user).append('\'');
        str.append(')');
        return str.toString();
    }
}
