package com.infomaximum.network.transport.http.http;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.controller.filter.AuthFilter;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.builder.filter.BuilderFilter;
import com.infomaximum.network.transport.http.http.utils.TestContentUtils;
import org.junit.jupiter.api.*;

import java.util.UUID;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FilterHttpRequestTest {

    private static final int port = 8099;

    private Network network;

    @BeforeAll
    public void init() throws Exception {
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(new BuilderHttpConnector(port))
                                .withJspPath("webapp/views")
                                .addFilter(new BuilderFilter(AuthFilter.class, "/jsp/*"))
                )
                .build();
    }

    @Test
    public void jspTest1() throws Exception {
        String message = UUID.randomUUID().toString();

        int statusCode1 = TestContentUtils.getStatusCode(port, "/jsp/?message=" + message);
        Assertions.assertEquals(401, statusCode1);

        int statusCode2 = TestContentUtils.getStatusCode(port, "/jsp/?auth=xxx&message=" + message);
        Assertions.assertEquals(200, statusCode2);
    }

    @AfterAll
    public void destroy() throws Exception {
        network.close();
        network=null;
    }
}
