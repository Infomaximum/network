package com.infomaximum.network;

import com.infomaximum.network.event.NetworkListener;
import com.infomaximum.network.protocol.Protocol;
import com.infomaximum.network.protocol.ProtocolUtils;
import com.infomaximum.network.protocol.standard.packet.RequestPacket;
import com.infomaximum.network.session.TransportSession;
import com.infomaximum.network.struct.info.NetworkInfo;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.TransportListener;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class NetworkImpl implements Network, TransportListener {

    private final static Logger log = LoggerFactory.getLogger(NetworkImpl.class);

    public volatile static NetworkImpl instance = null;

    private final Class<? extends RequestPacket> extensionRequestPacket;

    private final List<Transport> transports;

    private final List<Protocol> protocols;

    private final Set<NetworkListener> listeners;
    private final ConcurrentHashMap<Object, TransportSession> transportSessions;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public NetworkImpl(
            List<Protocol> protocols,
            Class<? extends RequestPacket> extensionRequestPacket,
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler
    ) {
        this.protocols = protocols;

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

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    @Override
    public void onConnect(Transport transport, Object channel, String remoteIpAddress) {
        try {
            String nameProtocol = ProtocolUtils.getWebSocketProtocol((WebSocketSession) channel);
            Protocol protocol = null;
            for (Protocol iProtocol : protocols) {
                if (iProtocol.getName().equals(nameProtocol)) {
                    protocol = iProtocol;
                    break;
                }
            }
            if (protocol == null) {
                throw new RuntimeException("Not support protocol, name: " + nameProtocol);
            }

            TransportSession transportSession = transportSessions.get(channel);
            if (transportSession != null) return;//Странное это дело...

            transportSession = protocol.onConnect(transport, channel);
            transportSessions.put(channel, transportSession);

            log.info("{} onConnect, ip: {}", transportSession.getSession(), remoteIpAddress);

            //Оповещаем подписчиков о новом подключении
            for (NetworkListener listener : listeners) {
                listener.onConnect(transportSession.getSession());
            }

        } catch (Exception e) {
            log.error("Error connect", e);
            onDisconnect(transport, channel, -1, e);
        }
    }

    @Override
    public void incomingMessage(Transport transport, Object channel, String message) {
        TransportSession threadSession = transportSessions.get(channel);
        threadSession.incomingMessage(message);
    }

    @Override
    public void onDisconnect(Transport transport, Object channel, int statusCode, Throwable throwable) {
        TransportSession transportSession = transportSessions.remove(channel);
        if (transportSession != null) {
            log.info("{} onDisconnect, code: {}, message: {}", transportSession.getSession(), statusCode, throwable.getMessage());
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

    @Override
    public NetworkInfo getInfo() {
        return new NetworkInfo(transports);
    }


    @Override
    public void close() {
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