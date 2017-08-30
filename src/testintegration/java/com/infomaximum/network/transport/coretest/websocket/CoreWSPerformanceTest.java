package com.infomaximum.network.transport.coretest.websocket;

import com.infomaximum.network.Network;
import com.infomaximum.network.packet.RequestPacket;
import com.infomaximum.network.packet.ResponsePacket;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Assert;
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

                        Future<Session> fut = client.connect(new WebSocketAdapter() {
                            @Override
                            public void onWebSocketText(String message) {
                                super.onWebSocketText(message);
                                try {
                                    ResponsePacket responsePacket = (ResponsePacket)network.parsePacket((JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(message));
                                    responseFuture.complete(responsePacket);
                                } catch (Exception e) {
                                    Assert.fail();
                                }
                            }

                            @Override
                            public void onWebSocketClose(int statusCode, String reason) {
                                super.onWebSocketClose(statusCode, reason);
                                if (!responseFuture.isDone())
                                    responseFuture.completeExceptionally(new Exception("Соединение закрыто"));
                            }

                            @Override
                            public void onWebSocketError(Throwable cause) {
                                super.onWebSocketError(cause);
                                if (!responseFuture.isDone())
                                    responseFuture.completeExceptionally(new Exception("Ошибка соединения", cause));
                            }
                        }, new URI("ws://localhost:"  + port + "/ws"));
                        Session session = fut.get();
                        countConnected.incrementAndGet();
                        log.info("connected: " + countConnected.get());


                        //TODO в это месте есть возмодный баг, порой все сависает
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
                        session.getRemote().sendString(requestPacket1.serialize());

                        ResponsePacket responsePacket1 = responseFuture.get(1, TimeUnit.MINUTES);
                        Assert.assertEquals(requestPacket1.getId(), responsePacket1.getId());

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
        Assert.assertEquals(countSuccess, taskList.size());
    }
}
