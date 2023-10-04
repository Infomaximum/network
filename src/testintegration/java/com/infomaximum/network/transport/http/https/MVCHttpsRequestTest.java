package com.infomaximum.network.transport.http.https;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.struct.info.HttpsConnectorInfo;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.builder.connector.BuilderJHttpsConnector;
import com.infomaximum.network.transport.http.http.utils.TestContentSslUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Created by kris on 26.08.16.
 * <p>
 * Тест проверяющий, что на запрос приходит ответ
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MVCHttpsRequestTest extends TestHttpsRequest {

    private final static Logger log = LoggerFactory.getLogger(MVCHttpsRequestTest.class);
    private static final int port = 8099;

    @Test
    public void testMvcController() throws Exception {
        initKeyStore();
        try (Network network = buildNetwork(new BuilderJHttpsConnector(port)
                .withSslContext(keyStorePath.toAbsolutePath().toString())
                .resetExcludeProtocolsAndCipherSuites()
                .setKeyStorePassword(PASSWORD)
                .build())) {
            TestContentSslUtils.testContent(port, "/test/ping", "pong", "TLS", keystore);
        }
    }

    //TODO Выключено при миграции на JUnit 5 - поправить
    //@Test( expected = SSLHandshakeException.class)
    public void testFailBecauseExcludeProtocol() throws Exception {
        initKeyStore();
        try (Network network = buildNetwork(new BuilderJHttpsConnector(port)
                .withSslContext(keyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setExcludeProtocols("SSLv2Hello", "TLSv1.2")
                .build())) {

            HttpsConnectorInfo actualConnectorInfo = (HttpsConnectorInfo) network.getInfo().getTransportsInfo().get(0).getConnectorsInfo().get(0);
            Assertions.assertFalse(actualConnectorInfo.containsSelectedProtocol("SSLv2Hello"));
            Assertions.assertFalse(actualConnectorInfo.containsSelectedProtocol("TLSv1.2"));

            TestContentSslUtils.testConnectionFail(port, "/test/ping", "TLSv1.2", keystore);
        }
    }

    @Test
    public void testExcludeCipherSuites() throws Exception {
        initKeyStore();
        try (Network network = buildNetwork(new BuilderJHttpsConnector(port)
                .withSslContext(keyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setExcludeCipherSuites("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256")
                .build())) {

            HttpsConnectorInfo actualConnectorInfo = (HttpsConnectorInfo) network.getInfo().getTransportsInfo().get(0).getConnectorsInfo().get(0);
            Assertions.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"));
            Assertions.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256.2"));
        }
    }

    @Test
    public void testAppendExcludeCipherSuitesAndProtocols() throws Exception {
        initKeyStore();
        try (Network network = buildNetwork(new BuilderJHttpsConnector(port)
                .withSslContext(keyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setExcludeCipherSuites("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256")
                .setExcludeProtocols("TLSv1.1")
                .addExcludeCipherSuites("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA")
                .addExcludeProtocols("TLSv1.2")
                .build())) {

            HttpsConnectorInfo actualConnectorInfo = (HttpsConnectorInfo) network.getInfo().getTransportsInfo().get(0).getConnectorsInfo().get(0);
            Assertions.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"));
            Assertions.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384"));
            Assertions.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"));
            Assertions.assertFalse(actualConnectorInfo.containsSelectedCipherSuite("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA"));

            Assertions.assertFalse(actualConnectorInfo.containsSelectedProtocol("TLSv1.1"));
            Assertions.assertFalse(actualConnectorInfo.containsSelectedProtocol("TLSv1.2"));
        }
    }

    @Test
    public void testTwoWayAuth() throws Exception {
        Path serverKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/server.p12")).toURI());
        Path clientKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/client.p12")).toURI());
        Path serverAndClientTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/ca_truststore")).toURI());

        try (Network network = buildNetwork(new BuilderJHttpsConnector(port)
                .withSslContext(serverKeyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setTrustStorePath(serverAndClientTrustStorePath.toAbsolutePath().toString())
                .build())) {

            TestContentSslUtils.testContentTwoWaySslAuthorization(port, "/test/ping", "pong", clientKeyStorePath, serverAndClientTrustStorePath, "TLS", PASSWORD);
        }
    }

    @Test
    public void testTwoWayAuthServerTruststoreDoesNotContainsClientCertificate() throws Exception {
        Path serverKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/server.p12")).toURI());
        Path clientKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/client.p12")).toURI());
        Path clientTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/ca_truststore")).toURI());
        Path serverTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/un_truststore")).toURI());

        try (Network network = buildNetwork(new BuilderJHttpsConnector(port)
                .withSslContext(serverKeyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setTrustStorePath(serverTrustStorePath.toAbsolutePath().toString())
                .build())) {

            TestContentSslUtils.testContentTwoWaySslAuthorization(port, "/test/ping", "pong", clientKeyStorePath, clientTrustStorePath, "TLS", PASSWORD);
        }
    }

    //TODO Выключено при миграции на JUnit 5 - поправить
    //@Test(expected = SSLHandshakeException.class)
    public void testFailTwoWayAuthBecauseClientTruststoreDoesNotContainsServerCertificate() throws Exception {
        Path serverKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/server.p12")).toURI());
        Path clientKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/client.p12")).toURI());
        Path clientTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/un_truststore")).toURI());
        Path serverTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/ca_truststore")).toURI());

        try (Network network = buildNetwork(new BuilderJHttpsConnector(port)
                .withSslContext(serverKeyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setTrustStorePath(serverTrustStorePath.toAbsolutePath().toString())
                .build())) {

            TestContentSslUtils.testContentTwoWaySslAuthorization(port, "/test/ping", "pong", clientKeyStorePath, clientTrustStorePath, "TLS", PASSWORD);
        }
    }

    //TODO Выключено при миграции на JUnit 5 - поправить
    //@Test(expected = SSLHandshakeException.class)
    public void testFailBecauseCertificateRevoked() throws Exception {
        Path serverKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/server.p12")).toURI());
        Path clientKeyStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/client.p12")).toURI());
        Path serverAndClientTrustStorePath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/ca_truststore")).toURI());

        Path crlPath = Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("httpstest/client_revoked.crl")).toURI());

        try (Network network = buildNetwork(new BuilderJHttpsConnector(port)
                .withSslContext(serverKeyStorePath.toAbsolutePath().toString())
                .setKeyStorePassword(PASSWORD)
                .setTrustStorePath(serverAndClientTrustStorePath.toAbsolutePath().toString())
                .setCrlPath(crlPath.toString())
                .build())) {

            TestContentSslUtils.testContentTwoWaySslAuthorization(port, "/test/ping", "pong", clientKeyStorePath, serverAndClientTrustStorePath, "TLS", PASSWORD);
        }
    }

    private static Network buildNetwork(BuilderHttpConnector builderHttpConnector) throws Exception {
        Network network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(builderHttpConnector)
//                                .withJspPath("webapp/views")
                )
                .build();
        log.info(network.getInfo().toString());
        return network;
    }
}
