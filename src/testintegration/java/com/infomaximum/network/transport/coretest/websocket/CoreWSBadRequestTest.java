package com.infomaximum.network.transport.coretest.websocket;

import com.infomaximum.network.protocol.standard.StandardProtocol;
import com.infomaximum.network.protocol.standard.packet.RequestPacket;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Assertions;

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

        //Калбек ответа
        CompletableFuture<Boolean> responseFuture = new CompletableFuture<Boolean>();

        WebSocketClient client = new WebSocketClient();
        client.start();

        ClientEndPoint clientEndPoint = new ClientEndPoint(responseFuture);
        URI serverURI = URI.create("ws://localhost:"  + port + "/ws");

        ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
        upgradeRequest.setSubProtocols(StandardProtocol.NAME);

        Future<Session> fut = client.connect(clientEndPoint, serverURI, upgradeRequest);

        //Ожидаем подключения
        Session session = fut.get();

        //Отправляем совоеобразный пакет пинга
        session.sendText(new RequestPacket(1, "бла бла", "ping", null).serialize(), Callback.NOOP);

        //Ждем разрыва соединения
        responseFuture.get(1, TimeUnit.MINUTES);

        session.close();//Закрываем соединение
    }

    public static class ClientEndPoint implements Session.Listener  {

        private final CompletableFuture<Boolean> responseFuture;

        public ClientEndPoint(CompletableFuture<Boolean> responseFuture) {
            this.responseFuture=responseFuture;
        }

        @Override
        public void onWebSocketText(String message) {
            responseFuture.completeExceptionally(new Exception("Пришел пакет хотя соединение должно было разорваться"));
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            responseFuture.complete(true);//Все хорошо, соединение разорвалось
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            Assertions.assertTrue(true);
        }
    }
}
