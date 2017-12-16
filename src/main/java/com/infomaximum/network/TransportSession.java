package com.infomaximum.network;

import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.struct.ISessionData;
import com.infomaximum.network.transport.Transport;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.13
 * Time: 22:44
 * To change this template use File | Settings | File Templates.
 */
public class TransportSession {

    private final static Logger log = LoggerFactory.getLogger(TransportSession.class);

    public final static long DEFAULT_REQUEST_TIMEOUT = 3L*60L*1000L;//Таймаут

    private final Session session;

    private final Network network;
    private final Transport transport;
    private final Object channel;

    /** итератор для id пакетов- запрашиваем о чем то клиент */
    private final AtomicLong nextIdPacketToQuestionClient = new AtomicLong(-1);
    private final Map<Long, CompletableFuture<ResponsePacket>> waitResponses = new ConcurrentHashMap<Long, CompletableFuture<com.infomaximum.network.packet.ResponsePacket>>();

    //Флаг определяеющий что мы в фазе рукопожатия
    private boolean isPhaseHandshake;

    public TransportSession(final Network network, final Transport transport, final Object channel, Class<? extends ISessionData> sessionDataClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.network=network;
        this.transport=transport;
        this.channel=channel;

        this.session = new Session(network, this, sessionDataClass);

        //Если есть обработчик рукопожатия, то ставим флаг
        isPhaseHandshake = (network.getHandshake()!=null);
    }

    protected boolean isPhaseHandshake() {
        return isPhaseHandshake;
    }

    public void completedPhaseHandshake(){
        isPhaseHandshake = false;
    }

    public void failPhaseHandshake(){
        try {
            transport.close(channel);
        } catch (Throwable ignore) {}
        destroyed();
    }

    public Session getSession() {
        return session;
    }

    /** Сюда приходят входящие пакеты из сети */
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
                CompletableFuture<ResponsePacket> response = getExecutePacket().exec(session, (com.infomaximum.network.packet.TargetPacket) packet);
                response.thenAccept(responsePacket -> {
                    if (packet.getType() == com.infomaximum.network.packet.TypePacket.REQUEST) {//Требуется ответ
                        if (responsePacket == null) {
                            log.error("Response packet is null");
                            try {
                                transport.close(channel);
                            } catch (Throwable ignore) {}
                            destroyed();
                        }
                        try {
                            transport.send(channel, responsePacket);
                        } catch (Throwable e) {
                            if (!(e instanceof IOException)) {
                                log.error("Exception", e);
                            }
                            try {
                                transport.close(channel);
                            } catch (Throwable ignore) {}
                            destroyed();
                        }
                    }
                });

//                response.handle((jsonObject, throwable) -> {
//                    if (throwable == null) {
//                        if (packet.getType() == com.infomaximum.network.packet.TypePacket.REQUEST) {//Требуется ответ
//                            transport.send(channel, com.infomaximum.network.packet.ResponsePacket.response((com.infomaximum.network.packet.RequestPacket) packet, network.getCodeResponse().SUCCESS(), response));
//                        }
//                    } else {
//                        if (throwable instanceof ResponseException)
//
//                        if (packet.getType() == com.infomaximum.network.packet.TypePacket.REQUEST) {//Требуется ответ
//                            transport.send(channel, com.infomaximum.network.packet.ResponsePacket.response((com.infomaximum.network.packet.RequestPacket) packet, responseException.getCode(), responseException.getDate()));
//                        }
//
//                        //Если это ошибка рукопожатия, то надо рвать соединение
//                        if (throwable instanceof HandshakeException) {
//                            try {
//                                transport.close(channel);
//                            } catch (Throwable ignore) {}
//                            destroyed();
//                        }
//                    }
//
//                    return null;
//                });
//
//                response.thenAccept(jsonObject -> {
//                    if (packet.getType() == com.infomaximum.network.packet.TypePacket.REQUEST) {//Требуется ответ
//                        transport.send(channel, com.infomaximum.network.packet.ResponsePacket.response((com.infomaximum.network.packet.RequestPacket) packet, network.getCodeResponse().SUCCESS(), response));
//                    }
//                }).exceptionally(throwable -> {
//                    if (packet.getType() == com.infomaximum.network.packet.TypePacket.REQUEST) {//Требуется ответ
//                        transport.send(channel, com.infomaximum.network.packet.ResponsePacket.response((com.infomaximum.network.packet.RequestPacket) packet, responseException.getCode(), responseException.getDate()));
//                    }
//
//                    //Если это ошибка рукопожатия, то надо рвать соединение
//                    if (throwable instanceof HandshakeException) {
//                        try {
//                            transport.close(channel);
//                        } catch (Throwable ignore) {}
//                        destroyed();
//                    }
//                });
            }
        } catch (Exception e) {
            log.error("{} Ошибка обработки входящего пакета: ", session, e);
            try { transport.close(channel); } catch (IOException ignore) {}
            destroyed();
        }
    }

    public void sendAsync(String controller, String method, JSONObject data) throws IOException {
        transport.send(channel, new com.infomaximum.network.packet.AsyncPacket(controller, method, data));
    }

    public com.infomaximum.network.packet.ResponsePacket sendRequest(String controller, String method, JSONObject data) throws TimeoutException, ExecutionException, InterruptedException, IOException {
        return sendRequest(controller, method, data, DEFAULT_REQUEST_TIMEOUT);
    }

    public com.infomaximum.network.packet.ResponsePacket sendRequest(String controller, String method, JSONObject data, long timeout) throws TimeoutException, ExecutionException, InterruptedException, IOException {
        long id = nextIdPacketToQuestionClient.getAndIncrement();

        CompletableFuture<com.infomaximum.network.packet.ResponsePacket> future = new CompletableFuture<com.infomaximum.network.packet.ResponsePacket>();
        waitResponses.put(id, future);

        try {
            //Отправляем пакет на запрос
            transport.send(channel, new com.infomaximum.network.packet.RequestPacket(id, controller, method, data));

            //Дожидаемся ответа
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } finally {
            //Чистим
            waitResponses.remove(id);
        }
    }

    /**
     * Возврощаем обработчика пакетов
     * @return
     */
    protected IExecutePacket getExecutePacket() {
        if (isPhaseHandshake) {
            return network.getHandshake();
        } else {
            return network.getExecutePacket();
        }
    }

    protected void destroyed() {
        try { transport.close(channel); } catch (Throwable ignore) {}
    }
}
