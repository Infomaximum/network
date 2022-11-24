package com.infomaximum.network.transport.http.builder.connector;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.struct.info.HttpConnectorInfo;
import com.infomaximum.network.struct.info.HttpsConnectorInfo;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.util.function.Supplier;

public class BuilderHttpsConnector extends BuilderHttpConnector {

    private final static Logger log = LoggerFactory.getLogger(BuilderHttpsConnector.class);

    private SslContextFactory.Server sslContextFactory;

    public BuilderHttpsConnector(int port) {
        super(port);
    }

    /**
     * Support format: p12
     * @param keyStorePath
     * @return
     */
    public BuilderSslContextFactory withSslContext(String keyStorePath) {
        return new BuilderSslContextFactory(this, keyStorePath);
    }

    @Override
    public Connector build(Server server) throws NetworkException {
        if (sslContextFactory == null) throw new NetworkException("Not init ssl key");

        HttpConfiguration httpsConfig = new HttpConfiguration();
        if (requestHeaderSize != null) {
            httpsConfig.setRequestHeaderSize(requestHeaderSize);
        }
        if (responseHeaderSize != null) {
            httpsConfig.setResponseHeaderSize(responseHeaderSize);
        }
        //todo Возможно, стоит реализовать свой кастомайзер, чтобы в реквест передавались только необходимые атрибуты
        httpsConfig.addCustomizer(new SecureRequestCustomizer());
        httpsConfig.setSecureScheme( "https" );
        httpsConfig.setSecurePort( port );

        ServerConnector connector = new ServerConnector(
                server,
                new SslConnectionFactory(
                        sslContextFactory,
                        "http/1.1"
                ),
                new HttpConnectionFactory(httpsConfig));
        connector.setPort( port );

//        log.info(sslContextFactory.dump());
        return connector;
    }

    public Supplier<? extends HttpConnectorInfo> getInfoSupplier() {
        return () -> new HttpsConnectorInfo(host, port, sslContextFactory.getSelectedProtocols(), sslContextFactory.getSelectedCipherSuites());
    }

    public class BuilderSslContextFactory {

        private final BuilderHttpsConnector builderHttpsConnector;
        private boolean validatePeerCerts = false;

        private BuilderSslContextFactory(BuilderHttpsConnector builderHttpsConnector, String keyStorePath) {
            this.builderHttpsConnector = builderHttpsConnector;

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(keyStorePath);

            this.builderHttpsConnector.sslContextFactory = sslContextFactory;
        }

        /**
         * Особенность, по умолчанию создается ssl-фабрика без уязвимых протоколов, но в каких то ситуация нам необходима
         * возможность подключения на небезопасных протоколах
         * https://stackoverflow.com/questions/52565445/tlsv1-support-in-embedded-jetty-server
         * @return
         */
        public BuilderSslContextFactory resetExcludeProtocolsAndCipherSuites() {
            sslContextFactory.setExcludeCipherSuites();
            sslContextFactory.setExcludeProtocols();
            return this;
        }

        public BuilderSslContextFactory setKeyStorePassword(String keyStorePassword) {
            builderHttpsConnector.sslContextFactory.setKeyStorePassword(keyStorePassword);
            return this;
        }

        public BuilderSslContextFactory setTrustStore(KeyStore keyStore) {
            SslContextFactory.Server sslContextFactory = builderHttpsConnector.sslContextFactory;

            sslContextFactory.setTrustStore(keyStore);
            sslContextFactory.setWantClientAuth(true);
            return this;
        }

        public BuilderSslContextFactory setTrustStorePassword(String trustStorePassword) {
            builderHttpsConnector.sslContextFactory.setTrustStorePassword(trustStorePassword);
            return this;
        }

        public BuilderSslContextFactory setTrustStorePath(String keyStore) {
            SslContextFactory.Server sslContextFactory = builderHttpsConnector.sslContextFactory;

            sslContextFactory.setTrustStorePath(keyStore);
            sslContextFactory.setWantClientAuth(true);
            return this;
        }

        public BuilderSslContextFactory setCrlPath(String crlPath) {
            builderHttpsConnector.sslContextFactory.setCrlPath(crlPath);
            validatePeerCerts = true;
            return this;
        }

        public BuilderSslContextFactory setIncludeProtocols(String... protocols) {
            builderHttpsConnector.sslContextFactory.setIncludeProtocols(protocols);
            return this;
        }

        public String[] getIncludeProtocols() {
            return builderHttpsConnector.sslContextFactory.getIncludeProtocols();
        }

        public BuilderSslContextFactory setExcludeProtocols(String... protocols) {
            builderHttpsConnector.sslContextFactory.setExcludeProtocols(protocols);
            return this;
        }

        public BuilderSslContextFactory addExcludeProtocols(String... protocols) {
            builderHttpsConnector.sslContextFactory.addExcludeProtocols(protocols);
            return this;
        }

        public String[] getExcludeProtocols() {
            return builderHttpsConnector.sslContextFactory.getExcludeProtocols();
        }

        public BuilderSslContextFactory setIncludeCipherSuites(String... cipherSuites) {
            builderHttpsConnector.sslContextFactory.setIncludeCipherSuites(cipherSuites);
            return this;
        }

        public String[] getIncludeCipherSuites() {
            return builderHttpsConnector.sslContextFactory.getIncludeCipherSuites();
        }

        public BuilderSslContextFactory setExcludeCipherSuites(String... cipherSuites) {
            builderHttpsConnector.sslContextFactory.setExcludeCipherSuites(cipherSuites);
            return this;
        }

        public BuilderSslContextFactory addExcludeCipherSuites(String... cipherSuites) {
            builderHttpsConnector.sslContextFactory.addExcludeCipherSuites(cipherSuites);
            return this;
        }

        public String[] getExcludeCipherSuites() {
            return builderHttpsConnector.sslContextFactory.getExcludeCipherSuites();
        }

        public BuilderHttpsConnector build() {
            builderHttpsConnector.sslContextFactory.setValidatePeerCerts(validatePeerCerts);
            return builderHttpsConnector;
        }
    }
}
