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
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
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
        int threads = 10000;
        Duration timeout = Duration.ofMinutes(5);

        HttpClient httpClient = new HttpClient();
        httpClient.setMaxRequestsQueuedPerDestination(threads);
        httpClient.setConnectTimeout(timeout.toMillis());
        httpClient.setIdleTimeout(timeout.toMillis());
        WebSocketClient client = new WebSocketClient(httpClient);
        client.setConnectTimeout(timeout.toMillis());
        client.setIdleTimeout(timeout);
        client.start();

        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        upgradeRequest.setSubProtocols(StandardProtocol.NAME);

        URI serverURI = URI.create("ws://localhost:"  + port + "/ws");

        ExecutorService executors = Executors.newVirtualThreadPerTaskExecutor();

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
                        int nowCountConnected = countConnected.incrementAndGet();
                        if (nowCountConnected%100 == 0) {
                            log.info("connected: " + nowCountConnected);
                        }

                        //Теперь ждем что-бы все подключились
                        while (true) {
                            if (fullConnect.get() || countConnected.get() == threads) {
                                fullConnect.set(true);
                                break;
                            } else {
                                Thread.sleep(100L);
                            }
                        }


                        //Отправляем своеобразный пакет пинга
                        RequestPacket requestPacket1 = new RequestPacket(1, "support", "ping", null);
                        session.sendText(requestPacket1.serialize(), Callback.from(() -> {}, responseFuture::completeExceptionally));

                        ResponsePacket responsePacket1 = responseFuture.get(5, TimeUnit.MINUTES);
                        Assertions.assertEquals(requestPacket1.getId(), responsePacket1.getId());

                        session.close();//Закрываем соединение

                        nowCountConnected = countConnected.decrementAndGet();
                        if (nowCountConnected%100 == 0) {
                            log.info("disconnect: " + nowCountConnected);
                        }

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

        //Ожидаем завершения всех потоков
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
                responseFuture.completeExceptionally(e);
                Assertions.fail();
            }
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            if (!responseFuture.isDone()) {
                responseFuture.completeExceptionally(new Exception("Соединение закрыто, code: " + statusCode + ", reason: " + reason));
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
