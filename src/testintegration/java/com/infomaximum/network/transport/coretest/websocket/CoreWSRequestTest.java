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

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
public class CoreWSRequestTest {

    public static void test(Network network, int port) throws Exception {
        WebSocketClient client = new WebSocketClient();
        client.start();

        //Калбек ответа
        CompletableFuture<ResponsePacket> responseFuture = new CompletableFuture<ResponsePacket>();

        Future<Session> fut = client.connect(new WebSocketAdapter(){
            @Override
            public void onWebSocketText(String message){
                super.onWebSocketText(message);
                try {
                    ResponsePacket responsePacket = (ResponsePacket)network.parsePacket((JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(message));
                    responseFuture.complete(responsePacket);
                } catch (Exception e) {
                    Assert.fail();
                }
            }

            @Override
            public void onWebSocketClose(int statusCode, String reason){
                super.onWebSocketClose(statusCode, reason);
                if (! responseFuture.isDone()) responseFuture.completeExceptionally(new Exception("Соединение закрыто"));
            }

            @Override
            public void onWebSocketError(Throwable cause){
                super.onWebSocketError(cause);
                if (! responseFuture.isDone()) responseFuture.completeExceptionally(new Exception("Ошибка соединения", cause));
            }
        }, new URI("ws://localhost:"  + port + "/ws"));

        //Ожидаем подключения
        Session session = fut.get();

        //Отправляем совоеобразный пакет пинга
        RequestPacket requestPacket = new RequestPacket(1, "support", "ping", null);
        session.getRemote().sendString(requestPacket.serialize());

        ResponsePacket responsePacket = responseFuture.get(1, TimeUnit.MINUTES);

        Assert.assertEquals(requestPacket.getId(), responsePacket.getId());
        Assert.assertEquals(network.getCodeResponse().SUCCESS(), responsePacket.getCode());

        session.close();//Закрываем соединение
    }
}
