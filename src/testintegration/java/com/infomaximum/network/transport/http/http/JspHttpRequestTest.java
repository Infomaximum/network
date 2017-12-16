package com.infomaximum.network.transport.http.http;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.http.utils.TestContentUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
public class JspHttpRequestTest {

    private static final int port = 8099;

    private Network network;

    @Before
    public void init() throws Exception {
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(port, SpringConfigurationMvc.class)
                                .withJspPath("webapp/views")
                )
                .build();
    }

    @Test
    public void jspTest1() throws Exception {
        String message = UUID.randomUUID().toString();
        String bodyContent = TestContentUtils.getContent(port, "/jsp/?message=" + message);

        Assert.assertNotEquals(-1, bodyContent.indexOf(message));
    }

    @After
    public void destroy() throws Exception {
        network.destroy();
        network=null;
    }
}
