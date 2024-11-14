package com.infomaximum.network.listener;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.event.HttpChannelListener;
import com.infomaximum.network.mvc.ResponseEntity;
import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.PacketHandler;
import com.infomaximum.network.protocol.standard.StandardProtocolBuilder;
import com.infomaximum.network.protocol.standard.packet.RequestPacket;
import com.infomaximum.network.protocol.standard.packet.ResponsePacket;
import com.infomaximum.network.session.Session;
import com.infomaximum.network.transport.coretest.websocket.CoreWSRequestTest;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.http.utils.TestContentUtils;
import net.minidev.json.JSONObject;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ListenerTest {

    private final static Logger log = LoggerFactory.getLogger(ListenerTest.class);

    private static final int port = 8099;

    private static Network network;
    private static TestHttpChannelListener listener;

    @BeforeAll
    public static void init() throws Exception {
        listener = new TestHttpChannelListener();
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(new BuilderHttpConnector(port))
                                .addListener(listener)
                )
                .withProtocol(new StandardProtocolBuilder()
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
                        ))
                .withUncaughtExceptionHandler((thread, throwable) -> {
                    Assertions.fail(throwable);
                })
                .build();
    }

    @Test
    public void simpleTest() throws Exception {
        for (int i = 0; i < 100; i++) {
            listener.reset();

            TestContentUtils.testContent(port, "/test/ping", "pong");

            //Интересная особенность хоть мы и получили ответ, но все события по завершению - могут отрабатывать с запазданием
            long startWait = System.currentTimeMillis();
            while (listener.onComplete == null && (System.currentTimeMillis() < startWait + 5 * 1000)) {
                log.debug("Wait complete, thread: " + i);
                Thread.sleep(100);
            }


            Assertions.assertNotNull(listener.onBeforeHandling);
            Assertions.assertNotNull(listener.onResponseBegin);
            Assertions.assertNotNull(listener.onResponseWrite);
            Assertions.assertNotEquals(0, listener.getOnResponseWriteSize());
            Assertions.assertNotNull(listener.onComplete);
        }
    }


    @Test
    public void fileTest() throws Exception {
        for (int i = 0; i < 100; i++) {
            listener.reset();

            int size = 100;
            byte[] bytes = TestContentUtils.getContentBytes(port, "/test/file?size=" + size);

            //Интересная особенность хоть мы и получили ответ, но все события по завершению - могут отрабатывать с запазданием
            long startWait = System.currentTimeMillis();
            while (listener.onComplete == null && (System.currentTimeMillis() < startWait + 5 * 1000)) {
                log.debug("Wait complete, thread: " + i);
                Thread.sleep(100);
            }

            Assertions.assertNotNull(listener.onBeforeHandling);
            Assertions.assertNotNull(listener.onResponseBegin);
            Assertions.assertNotNull(listener.onResponseWrite);
            Assertions.assertNotNull(listener.onComplete);

            Assertions.assertEquals(size, bytes.length);
            Assertions.assertNotEquals(0, listener.getOnResponseWriteSize());
        }
    }

    @Test
    public void websocketTest() throws Exception {
        for (int i = 0; i < 100; i++) {
            listener.reset();

            CoreWSRequestTest.test(network, port);

            //Интересная особенность хоть мы и получили ответ, но все события по завершению - могут отрабатывать с запазданием
            long startWait = System.currentTimeMillis();
            while (listener.onComplete == null && (System.currentTimeMillis() < startWait + 5 * 1000)) {
                log.debug("Wait complete, thread: " + i);
                Thread.sleep(100);
            }


            Assertions.assertNotNull(listener.onBeforeHandling);
            Assertions.assertNotNull(listener.onResponseBegin);
            Assertions.assertNotNull(listener.onResponseWrite);
            //Assertions.assertNotEquals(0, listener.getOnResponseWriteSize()); В случае вебсокетного соединения посчитать не получается
            Assertions.assertNotNull(listener.onComplete);

        }
    }


    @AfterAll
    public static void destroy() throws Exception {
        network.close();
        network = null;
        listener = null;
    }

    static class TestHttpChannelListener implements HttpChannelListener {

        private volatile Request onBeforeHandling;
        private volatile Request onResponseBegin;

        private volatile Request onResponseWrite;
        private volatile List<Integer> onResponseWritePart;

        private volatile Request onComplete;

        @Override
        public void onBeforeHandling(Request request) {
            if (onBeforeHandling != null) {
                throw new IllegalArgumentException();
            }
            onBeforeHandling = request;
        }

        @Override
        public void onResponseBegin(Request request, int status, HttpFields headers) {
            if (onResponseBegin != null) {
                throw new IllegalArgumentException();
            }
            onResponseBegin = request;
        }

        @Override
        public void onResponseWrite(Request request, boolean last, ByteBuffer content) {
            if (onResponseWrite == null) {
                onResponseWrite = request;
                onResponseWritePart = new ArrayList<>();
            } else if (onResponseWrite != request) {
                throw new IllegalArgumentException();
            }

            if (content != null) {//В случае вебсокетного соединения: content может быть null
                onResponseWritePart.add(content.capacity());
            }
        }

        @Override
        public void onComplete(Request request, int status, HttpFields headers, Throwable failure) {
            if (onComplete != null) {
                throw new IllegalArgumentException();
            }
            onComplete = request;
        }

        public Integer getOnResponseWriteSize() {
            return onResponseWritePart.stream().mapToInt(integer -> integer).sum();
        }

        public void reset() {
            onBeforeHandling = null;
            onResponseBegin = null;

            onResponseWrite = null;
            onResponseWritePart = null;

            onComplete = null;
        }
    }
}
