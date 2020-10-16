package com.infomaximum.network.transport.http.websocket;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.mvc.ResponseEntity;
import com.infomaximum.network.protocol.standard.StandardProtocolBuilder;
import com.infomaximum.network.protocol.standard.handler.PacketHandler;
import com.infomaximum.network.protocol.standard.packet.RequestPacket;
import com.infomaximum.network.protocol.standard.packet.ResponsePacket;
import com.infomaximum.network.protocol.standard.packet.TargetPacket;
import com.infomaximum.network.session.Session;
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
 * <p>
 * Тест проверяющий, что на запрос приходит ответ
 */
public class WSRequestTest {

    private static final int port = 8099;

    private static Network network;

    @Test
    public void test() throws Exception {
        CoreWSRequestTest.test(network, port);
    }

    @BeforeClass
    public static void init() throws Exception {
        network = new BuilderNetwork()
                .withProtocol(new StandardProtocolBuilder()
                        .withPacketHandler(
                                new PacketHandler.Builder() {
                                    @Override
                                    public PacketHandler build(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
                                        return new PacketHandler() {
                                            @Override
                                            public CompletableFuture<ResponsePacket> exec(Session session, TargetPacket packet) {
                                                if (packet instanceof RequestPacket) {
                                                    CompletableFuture<ResponsePacket> completableFuture = new CompletableFuture<ResponsePacket>();
                                                    completableFuture.complete(ResponsePacket.response(
                                                            (RequestPacket) packet,
                                                            ResponseEntity.RESPONSE_CODE_OK,
                                                            new JSONObject()
                                                    ));
                                                    return completableFuture;
                                                } else {
                                                    return null;
                                                }
                                            }
                                        };
                                    }
                                }
                        ))
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(new BuilderHttpConnector(port))
                )
                .build();
    }

    @AfterClass
    public static void destroy() throws Exception {
        network.close();
        network = null;
    }
}
