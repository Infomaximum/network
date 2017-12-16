package com.infomaximum.network.transport.socket;

import com.infomaximum.network.Network;
import com.infomaximum.network.Session;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.exception.ResponseException;
import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.packet.*;
import com.infomaximum.network.transport.socket.builder.SocketBuilderTransport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
public class SocketRequestTest {

    private static int randomPort = 8081;

    private static Network network;

    @BeforeClass
    public static void init() throws Exception {
        network = new BuilderNetwork()
                .withExecutePacket(new IExecutePacket() {
                    @Override
                    public CompletableFuture<ResponsePacket> exec(Session session, TargetPacket packet) throws ResponseException {
                        CompletableFuture<ResponsePacket> completableFuture = new CompletableFuture<>();
                        completableFuture.complete(null);
                        return completableFuture;
                    }
                })
                .withTransport(new SocketBuilderTransport(randomPort))
                .build();
    }


    @Test
    public void projectTest() throws Exception {

        Socket socket = new Socket("localhost", randomPort);


        OutputStream out = socket.getOutputStream();

        //Отправляем совоеобразный пакет пинга
        out.write(new RequestPacket(1, "support", "ping", null).serialize().getBytes());
        out.write(new byte[] { 0 });
        out.flush();

        //Отправляем совоеобразный пакет пинга
        out.write(new RequestPacket(2, "support", "ping", null).serialize().getBytes());
        out.write(new byte[] { 0 });
        out.flush();

        //Ожидаем ответа
        try (InputStream in = socket.getInputStream()){
            try (InputStreamReader inr = new InputStreamReader(in, "UTF-8")){
                try(BufferedReader br = new BufferedReader(inr)){
//                br.readLine()
                }
            }
        }



//        int responseLen = in.read();
//        byte[] response = new byte[responseLen];
//        in.read(response);
        socket.close();//Закрываем соединение

//        JSONObject incoming = (JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(response);
//        Packet responsePacket = Network.instance.parsePacket(incoming);

//        Assert.assertEquals(responsePacket.getType(), TypePacket.RESPONSE);
//        Assert.assertEquals(requestPacket.getId(), ((ResponsePacket)responsePacket).getId());
    }

    @AfterClass
    public static void destroy() throws Exception {
        network.destroy();
        network=null;
    }
}
