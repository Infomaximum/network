package com.infomaximum.network.transport.http.http;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.http.utils.TestContentUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
public class StaticHttpRequestTest {

    private static final int port = 8099;

    private Network network;

    @Before
    public void init() throws Exception {
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(new BuilderHttpConnector(port))
                                .withJspPath("webapp/views")
                )
                .build();
    }


    @Test
    public void staticFileTest1() throws Exception {
        TestContentUtils.testContent(port, "/static/internal.1.txt", "/webapp/static/internal.1.txt");
    }

    @Test
    public void staticFileTest2() throws Exception {
        TestContentUtils.testContent(port, "/static/1/internal.2.txt", "webapp/static/1/internal.2.txt");
    }

    @After
    public void destroy() throws Exception {
        network.close();
        network=null;
    }
}
