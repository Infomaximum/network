package com.infomaximum.network.transport.http.websocket;

import com.infomaximum.network.Session;
import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.exception.ResponseException;
import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.packet.TargetPacket;
import com.infomaximum.network.struct.TestCodeResponse;
import com.infomaximum.network.transport.coretest.websocket.CoreWSPerformanceTest;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import net.minidev.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kris on 29.08.16.
 */
public class WSPerformanceTest {

    private final static Logger log = LoggerFactory.getLogger(WSPerformanceTest.class);

    private static final int port=8099;

    private static Network network;

    @BeforeClass
    public static void init() throws Exception {
        network = new BuilderNetwork().withExecutePacket(new IExecutePacket() {
            @Override
            public JSONObject exec(Session session, TargetPacket packet) throws ResponseException {
                return new JSONObject();
            }
        })
                .withTransport(new HttpBuilderTransport(port, SpringConfigurationMvc.class))
                .withCodeResponse(new TestCodeResponse())
                .build();
    }


    @Test
    public void projectTest() throws Exception {
        CoreWSPerformanceTest.test(network, port);
    }

    @AfterClass
    public static void destroy() throws Exception {
        network.destroy();
        network=null;
    }
}
