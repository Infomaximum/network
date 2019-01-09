package com.infomaximum.network.transport.http.builder.connector;

import com.infomaximum.network.exception.NetworkException;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuilderHttpsConnector extends BuilderHttpConnector {

    private final static Logger log = LoggerFactory.getLogger(BuilderHttpsConnector.class);

    private SslContextFactory sslContextFactory;

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

    public class BuilderSslContextFactory {

        private final BuilderHttpsConnector builderHttpsConnector;

        private BuilderSslContextFactory(BuilderHttpsConnector builderHttpsConnector, String keyStorePath) {
            this.builderHttpsConnector = builderHttpsConnector;
            this.builderHttpsConnector.sslContextFactory = new SslContextFactory(keyStorePath);
        }

        public BuilderSslContextFactory setKeyStorePassword(String keyStorePassword) {
            builderHttpsConnector.sslContextFactory.setKeyStorePassword(keyStorePassword);
            return this;
        }

        public BuilderSslContextFactory setIncludeProtocols(String... protocols) {
            builderHttpsConnector.sslContextFactory.setIncludeProtocols(protocols);
            return this;
        }

        public BuilderSslContextFactory setExcludeProtocols(String... protocols) {
            builderHttpsConnector.sslContextFactory.setExcludeProtocols(protocols);
            return this;
        }

        public BuilderSslContextFactory setIncludeCipherSuites(String... cipherSuites) {
            builderHttpsConnector.sslContextFactory.setIncludeCipherSuites(cipherSuites);
            return this;
        }

        public BuilderSslContextFactory setExcludeCipherSuites(String... cipherSuites) {
            builderHttpsConnector.sslContextFactory.setExcludeCipherSuites(cipherSuites);
            return this;
        }

        public BuilderHttpsConnector build() {
            return builderHttpsConnector;
        }
    }
}
