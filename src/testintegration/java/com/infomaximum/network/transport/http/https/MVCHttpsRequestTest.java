package com.infomaximum.network.transport.http.https;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpsConnector;
import com.infomaximum.network.transport.http.http.utils.TestContentSslUtils;
import org.junit.After;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
public class MVCHttpsRequestTest extends TestHttpsRequest {

    private static final int port = 8099;

    private Network network;

    @After
    public void destroy() throws Exception {
        network.close();
        network = null;
    }

    @Test
    public void testMvcController() throws Exception {
        initKeyStore();
        buildNetwork(new BuilderHttpsConnector(port)
                .withSslContext(keyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .build());
        TestContentSslUtils.testContent(port, "/test/ping", "pong", "TLS", keystore);
    }

    @Test(expected = SSLHandshakeException.class)
    public void testFailBecauseExcludeProtocol() throws Exception {
        initKeyStore();
        buildNetwork(new BuilderHttpsConnector(port)
                .withSslContext(keyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setExcludeProtocols("SSLv2Hello", "TLSv1.2")
                .build());
        TestContentSslUtils.testConnectionFail(port, "/test/ping", "TLSv1.2", keystore);
    }

    private void buildNetwork(BuilderHttpConnector builderHttpConnector) throws Exception {
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(builderHttpConnector)
                                .withJspPath("webapp/views")
                )
                .build();
    }
}
