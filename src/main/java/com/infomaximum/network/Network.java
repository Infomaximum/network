package com.infomaximum.network;

import com.infomaximum.network.event.NetworkListener;
import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.external.handshake.Handshake;
import com.infomaximum.network.packet.*;
import com.infomaximum.network.struct.ISessionData;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.TransportListener;
import net.minidev.json.JSONAware;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by IntelliJ IDEA.
 * User: Vladimir Ulitin
 * Date: 01.02.12
 * Time: 21:00
 * Занятые порты:
 * 8081 - socket
 * 8082 - websocket
 * 8083 - ssl websocket
 * 8843 - PolicyServer
 */
public class Network implements TransportListener {

    private final static Logger log = LoggerFactory.getLogger(Network.class);

    public static Network instance;

    private final Handshake handshake;

    private final Class<? extends ISessionData> sessionDataClass;
    private final IExecutePacket executePacket;

    private final Class<? extends RequestPacket> extensionRequestPacket;

    private final List<Transport> transports;

    private final Set<NetworkListener> listeners;
    private final ConcurrentHashMap<Object, TransportSession> transportSessions;

    private final ManagerSession managerSession;

    public Network(ManagerSession managerSession,
                   Handshake handshake,
                   Class<? extends ISessionData> sessionDataClass,
                   Class<? extends RequestPacket> extensionRequestPacket,
                   IExecutePacket executePacket
    ) throws Exception {
        this.handshake=handshake;

        this.sessionDataClass=sessionDataClass;
        this.executePacket=executePacket;

        this.extensionRequestPacket=extensionRequestPacket;

        this.transports = new CopyOnWriteArrayList<Transport>();

        this.listeners = new CopyOnWriteArraySet<NetworkListener>();
        this.transportSessions = new ConcurrentHashMap<Object, TransportSession>();

        this.managerSession = managerSession;
        if (managerSession != null) this.addNetworkListener(managerSession);

        if (instance!=null) throw new RuntimeException("Network is not singleton");
        instance = this;
    }

    public void registerTransport(Transport transport) {
        transports.add(transport);
    }

    protected Handshake getHandshake() {
        return handshake;
    }

    protected IExecutePacket getExecutePacket() {
        return executePacket;
    }

    public ManagerSession getManagerSession() {
        return managerSession;
    }

    @Override
    public void onConnect(Transport transport, Object channel, String remoteIpAddress) {
        try {
            TransportSession transportSession = transportSessions.get(channel);
            if (transportSession != null) return;//Странное это дело...

            transportSession = new TransportSession(this, transport, channel, sessionDataClass);
            transportSessions.put(channel, transportSession);

            log.info("{} onConnect, ip: {}", transportSession.getSession(), remoteIpAddress);

            //Оповещаем подписчиков о новом подключении
            for (NetworkListener listener : listeners) {
                listener.onConnect(transportSession.getSession());
            }

            //Начинаем фазу рукопожатия
            if (handshake == null) {
                //Прикольно у нас нет обработчика рукопожатий, сразу считаем что оно свершилось)
                onHandshake(transportSession.getSession());
            } else {
                handshake.onPhaseHandshake(transportSession.getSession());
            }
        } catch (Exception e) {
            log.error("Error connect", e);
            onDisconnect(transport, channel, -1, e);
        }
    }

    @Override
    public void incomingPacket(Transport transport, Object channel, Packet packet) {
        TransportSession threadSession = transportSessions.get(channel);
        threadSession.incomingPacket(packet);
    }

    protected void onHandshake(Session session) {

        //Оповещаем подписчиков
        for (NetworkListener listener: listeners) {
            listener.onHandshake(session);
        }
    }

    protected void onLogin(Session session, Serializable user){
        //Оповещаем подписчиков
        for (NetworkListener listener: listeners) {
            listener.onLogin(session, user);
        }
    }

    protected void onLogout(Session session, Serializable user){
        //Оповещаем подписчиков
        for (NetworkListener listener: listeners) {
            listener.onLogout(session, user);
        }
    }

    @Override
    public void onDisconnect(Transport transport, Object channel, int statusCode, Throwable throwable) {
        TransportSession transportSession = transportSessions.remove(channel);
        if (transportSession!=null) {
            log.info("{} onDisconnect, {}, exception: {}", transportSession.getSession(), statusCode, throwable);

            if (transportSession.getSession().isLogin()) {
                transportSession.getSession().logout();//Обязательно делаем logout
            }
            transportSession.destroyed();

            //Оповещаем подписчиков
            for (NetworkListener listener: listeners) {
                listener.onDisconnect(transportSession.getSession());
            }
        }
    }

    public void addNetworkListener(NetworkListener listener) {
        listeners.add(listener);
    }

    public void removeNetworkListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    public Packet parsePacket(JSONObject parse) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        TypePacket type = TypePacket.get(parse.getAsNumber("type").intValue());
        if (type==TypePacket.ASYNC) {
            String controller = parse.getAsString("controller");
            String action = parse.getAsString("action");
            JSONObject data = (JSONObject) parse.get("data");
            return new AsyncPacket(controller, action, data);
        } else if (type == TypePacket.REQUEST) {
            if (extensionRequestPacket==null) {
                return new RequestPacket(parse);
            } else {
                return extensionRequestPacket.getConstructor(JSONObject.class).newInstance(parse);
            }
        } else if (type == TypePacket.RESPONSE) {
            long id = parse.getAsNumber("id").longValue();
            JSONObject data = (JSONObject) parse.get("data");
            JSONAware dataException = (JSONAware) parse.get("error");
            return new ResponsePacket(id, data, dataException);
        } else {
            throw new RuntimeException("Not support type packet: " + type);
        }
    }


    public void destroy() {
        if (managerSession!=null) {
            removeNetworkListener(managerSession);
            managerSession.destroyed();
        }
        while (transports.size()>0) {
            try {
                transports.remove(0).destroy();
            } catch (Exception e) {
                log.error("Error destroy connect", e);
            }
        }
        instance=null;
    }
}
