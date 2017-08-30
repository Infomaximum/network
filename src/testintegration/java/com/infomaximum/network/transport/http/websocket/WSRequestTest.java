package com.infomaximum.network.transport.http.websocket;

import com.infomaximum.network.Session;
import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.exception.ResponseException;
import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.packet.TargetPacket;
import com.infomaximum.network.struct.TestCodeResponse;
import com.infomaximum.network.transport.coretest.websocket.CoreWSRequestTest;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import net.minidev.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
                    public JSONObject exec(Session session, TargetPacket packet) throws ResponseException {
                        return null;
                    }
                })
                .withTransport(new HttpBuilderTransport(port, SpringConfigurationMvc.class))
                .withCodeResponse(new TestCodeResponse())
                .build();
    }


    @Test
    public void test() throws Exception {
        CoreWSRequestTest.test(network, port);
    }

    @AfterClass
    public static void destroy() throws Exception {
        network.destroy();
        network=null;
    }
}
