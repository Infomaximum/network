package com.infomaximum.network.manager.multi;

import com.infomaximum.network.Session;
import com.infomaximum.network.ManagerSession;
import com.infomaximum.network.packet.ResponsePacket;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by kris on 30.08.16.
 */
public class MultiManagerSession extends ManagerSession {

    private final static Logger log = LoggerFactory.getLogger(MultiManagerSession.class);

    private Map<Serializable, Set<Session>> userSessions;

    public MultiManagerSession() {
        userSessions = new ConcurrentHashMap<Serializable, Set<Session>>();
    }

    @Override
    public void onLogin(Session session, Serializable user) {
        Set<Session> sessions = userSessions.get(user);
        if (sessions==null) {
            synchronized (userSessions) {
                sessions = userSessions.get(user);
                if (sessions==null) {
                    sessions = new CopyOnWriteArraySet<Session>();
                    userSessions.put(user, sessions);
                }
            }
        }
        sessions.add(session);
    }

    @Override
    public void onLogout(Session session, Serializable user) {
        Set<Session> sessions = userSessions.get(user);
        if (sessions==null) return;
        sessions.remove(session);
        if (sessions.isEmpty()) {
            synchronized (userSessions) {
                userSessions.remove(user);
            }
        }
    }

    @Override
    public void sendAsyncIfConnect(Serializable user, String controller, String method, JSONObject data) {
        Set<Session> sessions = userSessions.get(user);
        if (sessions==null) return;
        for (Session session: sessions) {
            try {
                session.getTransportSession().sendAsync(controller, method, data);
            } catch (Exception e) {
                log.error("Error send async packet", e);
            }
        }
    }

    @Override
    public ResponsePacket sendRequest(Serializable user, String controller, String method, JSONObject data) throws TimeoutException, ExecutionException, InterruptedException {
        throw new UnsupportedOperationException("Не реализовано");
    }


    @Override
    public ResponsePacket sendRequest(Serializable user, String controller, String method, JSONObject data, long timeout) throws TimeoutException, ExecutionException, InterruptedException {
        throw new UnsupportedOperationException("Не реализовано");
    }

    @Override
    public void destroyed() {
        userSessions=null;
    }

}
