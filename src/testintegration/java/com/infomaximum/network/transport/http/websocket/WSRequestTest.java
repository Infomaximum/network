package com.infomaximum.network.transport.http.websocket;

import com.infomaximum.network.session.Session;
import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.packet.RequestPacket;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.packet.TargetPacket;
import com.infomaximum.network.transport.coretest.websocket.CoreWSRequestTest;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import net.minidev.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
public class WSRequestTest {

    private static final int port=8099;

    private static Network network;

    @BeforeClass
    public static void init() throws Exception {
        network = new BuilderNetwork()
                .withExecutePacket(new IExecutePacket() {
                    @Override
                    public CompletableFuture<ResponsePacket> exec(Session session, TargetPacket packet) {
                        if (packet instanceof RequestPacket) {
                            CompletableFuture<ResponsePacket> completableFuture = new CompletableFuture<ResponsePacket>();
                            completableFuture.complete(ResponsePacket.responseAccept((RequestPacket)packet, new JSONObject()));
                            return completableFuture;
                        } else {
                            return null;
                        }
                    }
                })
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .withConnector(new BuilderHttpConnector(port))
                )
                .build();
    }


    @Test
    public void test() throws Exception {
        CoreWSRequestTest.test(network, port);
    }

    @AfterClass
    public static void destroy() throws Exception {
        network.close();
        network=null;
    }
}
