package com.infomaximum.network;

import com.infomaximum.network.event.NetworkListener;
import com.infomaximum.network.handler.PacketHandler;
import com.infomaximum.network.handler.handshake.Handshake;
import com.infomaximum.network.packet.*;
import com.infomaximum.network.session.Session;
import com.infomaximum.network.session.TransportSession;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.TransportListener;
import net.minidev.json.JSONAware;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class NetworkImpl implements Network, TransportListener {

    private final static Logger log = LoggerFactory.getLogger(NetworkImpl.class);

    public volatile static NetworkImpl instance = null;

    private final Handshake handshake;

    private final SessionDataBuilder sessionDataBuilder;
    private final PacketHandler packetHandler;

    private final Class<? extends RequestPacket> extensionRequestPacket;

    private final List<Transport> transports;

    private final Set<NetworkListener> listeners;
    private final ConcurrentHashMap<Object, TransportSession> transportSessions;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public NetworkImpl(Handshake handshake,
                       SessionDataBuilder sessionDataBuilder,
                       Class<? extends RequestPacket> extensionRequestPacket,
                       PacketHandler.Builder packetHandlerBuilder,
                       Thread.UncaughtExceptionHandler uncaughtExceptionHandler
    ) throws Exception {
        this.handshake = handshake;

        this.sessionDataBuilder = sessionDataBuilder;
        if (packetHandlerBuilder != null) {
            this.packetHandler = packetHandlerBuilder.build(this);
        } else {
            this.packetHandler = null;
        }

        this.extensionRequestPacket = extensionRequestPacket;

        this.transports = new CopyOnWriteArrayList<Transport>();

        this.listeners = new CopyOnWriteArraySet<NetworkListener>();
        this.transportSessions = new ConcurrentHashMap<Object, TransportSession>();

        if (uncaughtExceptionHandler == null) {
            this.uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    log.error("UncaughtException", e);
                }
            };
        } else {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        }

        if (instance != null) throw new RuntimeException("Network is not singleton");
        instance = this;
    }

    public void registerTransport(Transport transport) {
        transports.add(transport);
    }

    public Handshake getHandshake() {
        return handshake;
    }

    public SessionDataBuilder getSessionDataBuilder() {
        return sessionDataBuilder;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    @Override
    public void onConnect(Transport transport, Object channel, String remoteIpAddress) {
        try {
            TransportSession transportSession = transportSessions.get(channel);
            if (transportSession != null) return;//Странное это дело...

            transportSession = new TransportSession(this, transport, channel);
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

    public void onHandshake(Session session) {
        //Оповещаем подписчиков
        for (NetworkListener listener : listeners) {
            listener.onHandshake(session);
        }
    }

    @Override
    public void onDisconnect(Transport transport, Object channel, int statusCode, Throwable throwable) {
        TransportSession transportSession = transportSessions.remove(channel);
        if (transportSession != null) {
            log.info("{} onDisconnect, {}, exception: {}", transportSession.getSession(), statusCode, throwable);

            transportSession.destroyed();

            //Оповещаем подписчиков
            for (NetworkListener listener : listeners) {
                listener.onDisconnect(transportSession.getSession());
            }
        }
    }

    @Override
    public void addNetworkListener(NetworkListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeNetworkListener(NetworkListener listener) {
        listeners.remove(listener);
    }

    public Packet parsePacket(JSONObject parse) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        TypePacket type = TypePacket.get(parse.getAsNumber("type").intValue());
        if (type == TypePacket.ASYNC) {
            String controller = parse.getAsString("controller");
            String action = parse.getAsString("action");
            JSONObject data = (JSONObject) parse.get("data");
            return new AsyncPacket(controller, action, data);
        } else if (type == TypePacket.REQUEST) {
            if (extensionRequestPacket == null) {
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

    @Override
    public void close() throws Exception {
        while (transports.size() > 0) {
            try {
                transports.remove(0).destroy();
            } catch (Exception e) {
                log.error("Error destroy connect", e);
            }
        }
        instance = null;
    }
}

