package com.infomaximum.network.transport.http.websocket;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.coretest.websocket.CoreWSBadRequestTest;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на ошибочный пакет соединение рвется
 */
public class WSBadRequestTest {

    private static final int port=8099;

    private static Network network;

    @BeforeClass
    public static void init() throws Exception {
        network = new BuilderNetwork()
                .withTransport(new HttpBuilderTransport(port, SpringConfigurationMvc.class))
                .build();
    }


    @Test
    public void test() throws Exception {
        CoreWSBadRequestTest.test(port);
    }

    @AfterClass
    public static void destroy() throws Exception {
        network.destroy();
        network=null;
    }
}
