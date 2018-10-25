package com.infomaximum.network.session;

import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.handler.PacketHandler;
import com.infomaximum.network.packet.Packet;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.struct.HandshakeData;
import com.infomaximum.network.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.13
 * Time: 22:44
 * To change this template use File | Settings | File Templates.
 */
public class TransportSession {

    public final static long DEFAULT_REQUEST_TIMEOUT = 3L * 60L * 1000L;//Таймаут

    private final static Logger log = LoggerFactory.getLogger(TransportSession.class);

    private final Session session;

    private final NetworkImpl network;
    private final Transport transport;
    private final Object channel;

    /**
     * итератор для id пакетов- запрашиваем о чем то клиент
     */
    private final AtomicLong nextIdPacketToQuestionClient = new AtomicLong(-1);
    private final Map<Long, CompletableFuture<ResponsePacket>> waitResponses = new ConcurrentHashMap<Long, CompletableFuture<com.infomaximum.network.packet.ResponsePacket>>();

    //Флаг определяеющий что мы в фазе рукопожатия
    private boolean isPhaseHandshake;

    public TransportSession(final NetworkImpl network, final Transport transport, final Object channel) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.network = network;
        this.transport = transport;
        this.channel = channel;

        this.session = new Session(network, this);

        //Проверяем наличие фазы рукопожатия
        if (network.getHandshake() != null) {
            isPhaseHandshake = true;
        } else {
            isPhaseHandshake = false;
            network.onHandshake(session);
        }
    }

    protected boolean isPhaseHandshake() {
        return isPhaseHandshake;
    }

    public void completedPhaseHandshake(HandshakeData handshakeData) {
        isPhaseHandshake = false;
        session.initHandshakeData(handshakeData);
        network.onHandshake(session);
    }

    public void failPhaseHandshake(ResponsePacket responsePacket) {
        try {
            if (responsePacket != null) {
                transport.send(channel, responsePacket).get();
            }
            transport.close(channel);
        } catch (Throwable ignore) {
        }
        destroyed();
    }

    public Session getSession() {
        return session;
    }

    /**
     * Сюда приходят входящие пакеты из сети
     */
    public void incomingPacket(com.infomaximum.network.packet.Packet packet) {
        try {
            if (packet.getType() == com.infomaximum.network.packet.TypePacket.RESPONSE) {
                com.infomaximum.network.packet.ResponsePacket responsePacket = (com.infomaximum.network.packet.ResponsePacket) packet;
                //Пришел ответ на запрос
                CompletableFuture future = waitResponses.remove(responsePacket.getId());
                if (future == null) {
                    log.error("nothing answer: " + packet.toString());
                } else {
                    future.complete(responsePacket);
                }
            } else {
                getPacketHandler()
                        .exec(session, (com.infomaximum.network.packet.TargetPacket) packet)
                        .thenAccept(responsePacket -> {
                            if (packet.getType() == com.infomaximum.network.packet.TypePacket.REQUEST) {//Требуется ответ
                                if (responsePacket == null) {
                                    log.error("Response packet is null");
                                    try {
                                        transport.close(channel);
                                    } catch (Throwable ignore) {
                                    }
                                    destroyed();
                                }
                                try {
                                    send(responsePacket);
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
                        });
            }
        } catch (Exception e) {
            log.error("{} Ошибка обработки входящего пакета: ", session, e);
            try {
                transport.close(channel);
            } catch (IOException ignore) {
            }
            destroyed();
        }
    }

    public void send(Packet packet) throws IOException {
        transport.send(channel, packet);
    }

    /**
     * Возврощаем обработчика пакетов
     *
     * @return
     */
    protected PacketHandler getPacketHandler() {
        if (isPhaseHandshake) {
            return network.getHandshake();
        } else {
            return network.getPacketHandler();
        }
    }

    public void destroyed() {
        try {
            transport.close(channel);
        } catch (Throwable ignore) {
        }
    }
}
