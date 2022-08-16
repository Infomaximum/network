package com.infomaximum.network.transport.http.http;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.http.utils.TestContentUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Created by kris on 26.08.16.
 * <p>
 * Тест проверяющий, что на запрос приходит ответ
 */
public class JspHttpRequestTest {

    private static final int port = 8099;

    private Network network;

    @BeforeAll
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
    public void jspTest1() throws Exception {
        String message = UUID.randomUUID().toString();
        String bodyContent = TestContentUtils.getContent(port, "/jsp/?message=" + message);

        Assertions.assertNotEquals(-1, bodyContent.indexOf(message));
    }

    @AfterAll
    public void destroy() throws Exception {
        network.close();
        network = null;
    }
}
