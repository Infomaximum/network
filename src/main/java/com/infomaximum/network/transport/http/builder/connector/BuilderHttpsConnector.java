package com.infomaximum.network.transport.http.builder.connector;

import com.infomaximum.network.struct.info.HttpConnectorInfo;
import com.infomaximum.network.struct.info.HttpsConnectorInfo;
import com.infomaximum.network.utils.CertificateUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.function.Supplier;

public class BuilderHttpsConnector extends AbstractBuilderHttpsConnector {

    private final static Logger log = LoggerFactory.getLogger(BuilderHttpsConnector.class);

    private SslContextFactory.Server sslContextFactory;

    public BuilderHttpsConnector(int port) {
        super(port);
    }

    @Override
    protected SslContextFactory.Server getSslContextFactory() {
        return sslContextFactory;
    }

    public BuilderSslContextFactory withSslContext(byte[] certChain, byte[] privateKey) {
        return new BuilderSslContextFactory(this, certChain, privateKey);
    }

    public Supplier<? extends HttpConnectorInfo> getInfoSupplier() {
        return () -> new HttpsConnectorInfo(host, port, sslContextFactory.getSelectedProtocols(), sslContextFactory.getSelectedCipherSuites());
    }

    public class BuilderSslContextFactory {

        private final BuilderHttpsConnector builderJHttpsConnector;
        private boolean validatePeerCerts = false;

        private BuilderSslContextFactory(BuilderHttpsConnector builderJHttpsConnector, byte[] certChain, byte[] privateKey) {
            this.builderJHttpsConnector = builderJHttpsConnector;

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

            KeyStore keyStore = CertificateUtils.buildKeyStore(certChain, privateKey);
            sslContextFactory.setKeyStore(keyStore);

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

        public BuilderHttpsConnector build() {
            builderJHttpsConnector.sslContextFactory.setValidatePeerCerts(validatePeerCerts);
            return builderJHttpsConnector;
        }
    }
}
