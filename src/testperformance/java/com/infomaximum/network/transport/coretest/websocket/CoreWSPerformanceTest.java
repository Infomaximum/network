package com.infomaximum.network.transport.coretest.websocket;

import com.infomaximum.network.Network;
import com.infomaximum.network.protocol.standard.StandardProtocol;
import com.infomaximum.network.protocol.standard.packet.Packet;
import com.infomaximum.network.protocol.standard.packet.RequestPacket;
import com.infomaximum.network.protocol.standard.packet.ResponsePacket;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kris on 29.08.16.
 */
public class CoreWSPerformanceTest {

    private final static Logger log = LoggerFactory.getLogger(CoreWSPerformanceTest.class);

    public static void test(Network network, int port) throws Exception {
        WebSocketClient client = new WebSocketClient();
        client.setConnectTimeout(30L*1000L);
        client.start();

        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        upgradeRequest.setSubProtocols(StandardProtocol.NAME);

        URI serverURI = URI.create("ws://localhost:"  + port + "/ws");

        ExecutorService executors = Executors.newCachedThreadPool();

        int threads = 100;

        AtomicInteger countConnected = new AtomicInteger();
        AtomicBoolean fullConnect = new AtomicBoolean(false);

        List<FutureTask<Boolean>> taskList = new ArrayList<FutureTask<Boolean>>();
        for (int i=0; i<threads; i++) {
            FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    try {
                        //Калбек ответа
                        CompletableFuture<ResponsePacket> responseFuture = new CompletableFuture<ResponsePacket>();

                        ClientEndPoint clientEndPoint = new ClientEndPoint(responseFuture);

                        Future<Session> fut = client.connect(clientEndPoint, serverURI, upgradeRequest);

                        Session session = fut.get();
                        countConnected.incrementAndGet();
                        log.info("connected: " + countConnected.get());


                        //TODO в это месте есть возможный баг, порой все зависает
                        //Теперь ждем что-бы все подключились
                        while (true) {
                            if (fullConnect.get() || countConnected.get() == threads) {
//                                log.debug("is Full!!!!");
                                fullConnect.set(true);
                                break;
                            } else {
                                Thread.sleep(1000L);
                            }
                        }


                        //Отправляем совоеобразный пакет пинга
                        RequestPacket requestPacket1 = new RequestPacket(1, "support", "ping", null);
                        session.sendText(requestPacket1.serialize(), Callback.NOOP);

                        ResponsePacket responsePacket1 = responseFuture.get(1, TimeUnit.MINUTES);
                        Assertions.assertEquals(requestPacket1.getId(), responsePacket1.getId());

                        Thread.sleep(1000L);

                        session.close();//Закрываем соединение

                        log.info("disconnect: " + countConnected.decrementAndGet());

                        return true;
                    } catch (Exception e) {
                        log.error("Fail", e);
                        return false;
                    }
                }
            });

            taskList.add(futureTask);
            executors.execute(futureTask);
        }

        //Ожидем завершения всех потоков
        int countSuccess=0;
        for (FutureTask<Boolean> futureTask: taskList) {
            if (futureTask.get()) countSuccess++;
        }
        Assertions.assertEquals(countSuccess, taskList.size());
    }

    public static class ClientEndPoint implements Session.Listener {

        private final CompletableFuture<ResponsePacket> responseFuture;

        public ClientEndPoint(CompletableFuture<ResponsePacket> responseFuture) {
            this.responseFuture=responseFuture;
        }

        @Override
        public void onWebSocketText(String message) {
            try {
                ResponsePacket responsePacket = (ResponsePacket) (Packet.parse((JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(message)));
                responseFuture.complete(responsePacket);
            } catch (Exception e) {
                Assertions.fail();
            }
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            if (!responseFuture.isDone()) {
                responseFuture.completeExceptionally(new Exception("Соединение закрыто"));
            }
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            if (!responseFuture.isDone()) {
                responseFuture.completeExceptionally(new Exception("Ошибка соединения", cause));
            }
        }
    }
}
