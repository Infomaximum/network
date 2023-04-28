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

public class BuilderJHttpsConnector extends AbstractBuilderHttpsConnector {

    private final static Logger log = LoggerFactory.getLogger(BuilderJHttpsConnector.class);

    private SslContextFactory.Server sslContextFactory;

    public BuilderJHttpsConnector(int port) {
        super(port);
    }

    @Override
    protected SslContextFactory.Server getSslContextFactory() {
        return sslContextFactory;
    }

    /**
     * Support format: p12
     * @param keyStorePath
     * @return
     */
    public BuilderSslContextFactory withSslContext(String keyStorePath) {
        return new BuilderSslContextFactory(this, keyStorePath);
    }

    public Supplier<? extends HttpConnectorInfo> getInfoSupplier() {
        return () -> new HttpsConnectorInfo(host, port, sslContextFactory.getSelectedProtocols(), sslContextFactory.getSelectedCipherSuites());
    }

    public class BuilderSslContextFactory {

        private final BuilderJHttpsConnector builderJHttpsConnector;
        private boolean validatePeerCerts = false;

        private BuilderSslContextFactory(BuilderJHttpsConnector builderJHttpsConnector, String keyStorePath) {
            this.builderJHttpsConnector = builderJHttpsConnector;

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(keyStorePath);

            this.builderJHttpsConnector.sslContextFactory = sslContextFactory;
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
            builderJHttpsConnector.sslContextFactory.setKeyStorePassword(keyStorePassword);
            return this;
        }

        public BuilderSslContextFactory setTrustStore(KeyStore keyStore) {
            SslContextFactory.Server sslContextFactory = builderJHttpsConnector.sslContextFactory;

            sslContextFactory.setTrustStore(keyStore);
            sslContextFactory.setWantClientAuth(true);
            return this;
        }

        public BuilderSslContextFactory setTrustStorePassword(String trustStorePassword) {
            builderJHttpsConnector.sslContextFactory.setTrustStorePassword(trustStorePassword);
            return this;
        }

        public BuilderSslContextFactory setTrustStorePath(String keyStore) {
            SslContextFactory.Server sslContextFactory = builderJHttpsConnector.sslContextFactory;

            sslContextFactory.setTrustStorePath(keyStore);
            sslContextFactory.setWantClientAuth(true);
            return this;
        }

        public BuilderSslContextFactory setCrlPath(String crlPath) {
            builderJHttpsConnector.sslContextFactory.setCrlPath(crlPath);
            validatePeerCerts = true;
            return this;
        }

        public BuilderSslContextFactory setIncludeProtocols(String... protocols) {
            builderJHttpsConnector.sslContextFactory.setIncludeProtocols(protocols);
            return this;
        }

        public String[] getIncludeProtocols() {
            return builderJHttpsConnector.sslContextFactory.getIncludeProtocols();
        }

        public BuilderSslContextFactory setExcludeProtocols(String... protocols) {
            builderJHttpsConnector.sslContextFactory.setExcludeProtocols(protocols);
            return this;
        }

        public BuilderSslContextFactory addExcludeProtocols(String... protocols) {
            builderJHttpsConnector.sslContextFactory.addExcludeProtocols(protocols);
            return this;
        }

        public String[] getExcludeProtocols() {
            return builderJHttpsConnector.sslContextFactory.getExcludeProtocols();
        }

        public BuilderSslContextFactory setIncludeCipherSuites(String... cipherSuites) {
            builderJHttpsConnector.sslContextFactory.setIncludeCipherSuites(cipherSuites);
            return this;
        }

        public String[] getIncludeCipherSuites() {
            return builderJHttpsConnector.sslContextFactory.getIncludeCipherSuites();
        }

        public BuilderSslContextFactory setExcludeCipherSuites(String... cipherSuites) {
            builderJHttpsConnector.sslContextFactory.setExcludeCipherSuites(cipherSuites);
            return this;
        }

        public BuilderSslContextFactory addExcludeCipherSuites(String... cipherSuites) {
            builderJHttpsConnector.sslContextFactory.addExcludeCipherSuites(cipherSuites);
            return this;
        }

        public String[] getExcludeCipherSuites() {
            return builderJHttpsConnector.sslContextFactory.getExcludeCipherSuites();
        }

        public BuilderJHttpsConnector build() {
            builderJHttpsConnector.sslContextFactory.setValidatePeerCerts(validatePeerCerts);
            return builderJHttpsConnector;
        }
    }
}
