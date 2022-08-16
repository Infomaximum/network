package com.infomaximum.network.transport.http.http;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.http.utils.TestContentUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
public class MVCHttpRequestTest {

    private static final int port=8099;

    private static Network network;

    @BeforeAll
    public static void init() throws Exception {
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(new BuilderHttpConnector(port))
                                //.withJspPath("webapp/views")
                )
                .build();
    }


    @Test
    public void mvcControllerTest() throws Exception {
        TestContentUtils.testContent(port, "/test/ping", "pong");
    }

    @AfterAll
    public static void destroy() throws Exception {
        network.close();
        network=null;
    }
}
