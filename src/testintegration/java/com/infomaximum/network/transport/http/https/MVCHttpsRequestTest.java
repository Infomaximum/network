package com.infomaximum.network.transport.http.https;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.struct.info.HttpsConnectorInfo;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpsConnector;
import com.infomaximum.network.transport.http.http.utils.TestContentSslUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
public class MVCHttpsRequestTest extends TestHttpsRequest {

    private final static Logger log = LoggerFactory.getLogger(MVCHttpsRequestTest.class);
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

        HttpsConnectorInfo actualConnectorInfo = (HttpsConnectorInfo) network.getInfo().getTransportsInfo().get(0).getConnectorsInfo().get(0);
        Assert.assertFalse(actualConnectorInfo.containsSelectedProtocol("SSLv2Hello"));
        Assert.assertFalse(actualConnectorInfo.containsSelectedProtocol("TLSv1.2"));

        TestContentSslUtils.testConnectionFail(port, "/test/ping", "TLSv1.2", keystore);
    }

    @Test
    public void testExcludeCipherSuites() throws Exception {
        initKeyStore();
        buildNetwork(new BuilderHttpsConnector(port)
                .withSslContext(keyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setExcludeCipherSuites("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256")
                .build());

        HttpsConnectorInfo actualConnectorInfo = (HttpsConnectorInfo) network.getInfo().getTransportsInfo().get(0).getConnectorsInfo().get(0);
        Assert.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"));
        Assert.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256.2"));
    }

    @Test
    public void testAppendExcludeCipherSuitesAndProtocols() throws Exception {
        initKeyStore();
        buildNetwork(new BuilderHttpsConnector(port)
                .withSslContext(keyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setExcludeCipherSuites("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256")
                .setExcludeProtocols("TLSv1.1")
                .addExcludeCipherSuites("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA")
                .addExcludeProtocols("TLSv1.2")
                .build());

        HttpsConnectorInfo actualConnectorInfo = (HttpsConnectorInfo) network.getInfo().getTransportsInfo().get(0).getConnectorsInfo().get(0);
        Assert.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"));
        Assert.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384"));
        Assert.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"));
        Assert.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA"));

        Assert.assertFalse(actualConnectorInfo.containsSelectedProtocol("TLSv1.1"));
        Assert.assertFalse(actualConnectorInfo.containsSelectedProtocol("TLSv1.2"));
    }

    @Test
    public void testTwoWayAuth() throws Exception {
        Path serverKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/serverKeystore.jks")).toURI());
        Path serverTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/serverTruststore.jks")).toURI());
        Path clientKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/clientKeystore.jks")).toURI());
        Path clientTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/clientTruststore.jks")).toURI());

        buildNetwork(new BuilderHttpsConnector(port)
                .withSslContext(serverKeyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setTrustStorePath(serverTrustStorePath.toAbsolutePath().toString())
                .build());

        TestContentSslUtils.testContentTwoWaySslAuthorization(port, "/test/ping", "pong", clientKeyStorePath, clientTrustStorePath, "TLS", PASSWORD);
    }

    @Test(expected = SocketException.class)
    public void testFailTwoWayAuthBecauseServerTruststoreDoesNotContainsClientCertificate() throws Exception {
        Path serverKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/serverKeystore.jks")).toURI());
        Path clientKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/clientKeystore.jks")).toURI());
        Path clientTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/clientTruststore.jks")).toURI());

        Path serverTrustStorePath = clientTrustStorePath; // Задаем серверный trustStore, который не содержит клиентский сертификат

        buildNetwork(new BuilderHttpsConnector(port)
                .withSslContext(serverKeyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setTrustStorePath(serverTrustStorePath.toAbsolutePath().toString())
                .build());

        TestContentSslUtils.testContentTwoWaySslAuthorization(port, "/test/ping", "pong", clientKeyStorePath, clientTrustStorePath, "TLS", PASSWORD);
    }

    @Test(expected = SSLHandshakeException.class)
    public void testFailTwoWayAuthBecauseClientTruststoreDoesNotContainsServerCertificate() throws Exception {
        Path serverKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/serverKeystore.jks")).toURI());
        Path serverTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/serverTruststore.jks")).toURI());
        Path clientKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/clientKeystore.jks")).toURI());

        Path clientTrustStorePath = serverTrustStorePath; // Задаем клиенский trustStore, который не содержит серверный сертификат

        buildNetwork(new BuilderHttpsConnector(port)
                .withSslContext(serverKeyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setTrustStorePath(serverTrustStorePath.toAbsolutePath().toString())
                .build());

        TestContentSslUtils.testContentTwoWaySslAuthorization(port, "/test/ping", "pong", clientKeyStorePath, clientTrustStorePath, "TLS", PASSWORD);
    }

    private void buildNetwork(BuilderHttpConnector builderHttpConnector) throws Exception {
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(builderHttpConnector)
                                .withJspPath("webapp/views")
                )
                .build();
        log.info(network.getInfo().toString());
    }
}
