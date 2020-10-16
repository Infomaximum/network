package com.infomaximum.network.protocol.standard.session;

import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.standard.StandardProtocol;
import com.infomaximum.network.protocol.standard.handler.PacketHandler;
import com.infomaximum.network.protocol.standard.packet.Packet;
import com.infomaximum.network.protocol.standard.packet.ResponsePacket;
import com.infomaximum.network.session.Session;
import com.infomaximum.network.session.TransportSession;
import com.infomaximum.network.struct.HandshakeData;
import com.infomaximum.network.transport.Transport;
import net.minidev.json.JSONObject;
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
 */
public class StandardTransportSession extends TransportSession {

    public final static long DEFAULT_REQUEST_TIMEOUT = 3L * 60L * 1000L;//Таймаут

    private final static Logger log = LoggerFactory.getLogger(StandardTransportSession.class);

    private final Session session;

    /**
     * итератор для id пакетов- запрашиваем о чем то клиент
     */
    private final AtomicLong nextIdPacketToQuestionClient = new AtomicLong(-1);
    private final Map<Long, CompletableFuture<ResponsePacket>> waitResponses = new ConcurrentHashMap<Long, CompletableFuture<com.infomaximum.network.protocol.standard.packet.ResponsePacket>>();

    //Флаг определяеющий что мы в фазе рукопожатия
    private boolean isPhaseHandshake;

    public StandardTransportSession(StandardProtocol protocol, final Transport transport, final Object channel) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(protocol, transport, channel);

        this.session = new Session(this);

        //Проверяем наличие фазы рукопожатия
//		if (network.getHandshake() != null) {
//			isPhaseHandshake = true;
//		} else {
//			isPhaseHandshake = false;
////			network.onHandshake(session);
//		}
    }

    protected boolean isPhaseHandshake() {
        return isPhaseHandshake;
    }

    @Override
    public void completedPhaseHandshake(HandshakeData handshakeData) {
        isPhaseHandshake = false;
//		session.initHandshakeData(handshakeData);
//		network.onHandshake(session);
    }

    @Override
    public void failPhaseHandshake(IPacket packet) {
        ResponsePacket responsePacket = (ResponsePacket) packet;
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
    public void incomingPacket(JSONObject jPacket) {
        try {
            Packet packet = Packet.parse(jPacket);

            if (packet.getType() == com.infomaximum.network.protocol.standard.packet.TypePacket.RESPONSE) {
                com.infomaximum.network.protocol.standard.packet.ResponsePacket responsePacket = (com.infomaximum.network.protocol.standard.packet.ResponsePacket) packet;
                //Пришел ответ на запрос
                CompletableFuture future = waitResponses.remove(responsePacket.getId());
                if (future == null) {
                    log.error("nothing answer: " + packet.toString());
                } else {
                    future.complete(responsePacket);
                }
            } else {
                getPacketHandler()
                        .exec(session, (com.infomaximum.network.protocol.standard.packet.TargetPacket) packet)
                        .thenAccept(responsePacket -> {
                            if (packet.getType() == com.infomaximum.network.protocol.standard.packet.TypePacket.REQUEST) {//Требуется ответ
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

    /**
     * Возврощаем обработчика пакетов
     *
     * @return
     */
    protected PacketHandler getPacketHandler() {
//		if (isPhaseHandshake) {
//			return network.getHandshake();
//		} else {
        return ((StandardProtocol) protocol).getPacketHandler();
//		}
    }
}
