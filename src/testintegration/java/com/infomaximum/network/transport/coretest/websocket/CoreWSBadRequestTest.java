package com.infomaximum.network.transport.coretest.websocket;

import com.infomaximum.network.packet.RequestPacket;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.Assert;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на ошибочный пакет соединение рвется
 */
public class CoreWSBadRequestTest {

    public static void test(int port) throws Exception {

        WebSocketClient client = new WebSocketClient();
        client.start();

        //Калбек ответа
        CompletableFuture<Boolean> responseFuture = new CompletableFuture<Boolean>();

        Future<Session> fut = client.connect(new WebSocketAdapter(){
            @Override
            public void onWebSocketText(String message){
                super.onWebSocketText(message);
                responseFuture.completeExceptionally(new Exception("Пришел пакет хотя соединение должно было разорваться"));
            }

            @Override
            public void onWebSocketClose(int statusCode, String reason){
                super.onWebSocketClose(statusCode, reason);
                responseFuture.complete(true);//Все хорошо, соединение разорвалось
            }

            @Override
            public void onWebSocketError(Throwable cause){
                super.onWebSocketError(cause);
                Assert.assertTrue(true);
            }
        }, new URI("ws://localhost:"  + port + "/ws"));

        //Ожидаем подключения
        Session session = fut.get();

        //Отправляем совоеобразный пакет пинга
        session.getRemote().sendString(new RequestPacket(1, "бла бла", "ping", null).serialize());

        //Ждем разрыва соединения
        responseFuture.get(1, TimeUnit.MINUTES);

        session.close();//Закрываем соединение
    }
}
