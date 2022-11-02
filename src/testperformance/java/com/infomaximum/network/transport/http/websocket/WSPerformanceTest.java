package com.infomaximum.network.transport.http.websocket;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.mvc.ResponseEntity;
import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.PacketHandler;
import com.infomaximum.network.protocol.standard.StandardProtocolBuilder;
import com.infomaximum.network.protocol.standard.packet.RequestPacket;
import com.infomaximum.network.protocol.standard.packet.ResponsePacket;
import com.infomaximum.network.session.Session;
import com.infomaximum.network.transport.coretest.websocket.CoreWSPerformanceTest;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Created by kris on 29.08.16.
 */
public class WSPerformanceTest {

    private final static Logger log = LoggerFactory.getLogger(WSPerformanceTest.class);

    private static final int port = 8099;

    private static Network network;

    @Test
    public void projectTest() throws Exception {
        CoreWSPerformanceTest.test(network, port);
    }

    @BeforeAll
    public static void init() throws Exception {
        network = new BuilderNetwork()
                .withProtocol(
                        new StandardProtocolBuilder()
                                .withPacketHandler(
                                        new PacketHandler.Builder() {
                                            @Override
                                            public PacketHandler build(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
                                                return new PacketHandler() {
                                                    @Override
                                                    public CompletableFuture<IPacket[]> exec(Session session, IPacket packet) {
                                                        if (packet instanceof RequestPacket) {
                                                            CompletableFuture<IPacket[]> completableFuture = new CompletableFuture<IPacket[]>();
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
                                )
                )
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(new BuilderHttpConnector(port))
                )
                .build();
    }

    @AfterAll
    public static void destroy() throws Exception {
        network.close();
        network = null;
    }
}
