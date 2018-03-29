package com.infomaximum.network.transport.http.builder.connector;

import com.infomaximum.network.exception.NetworkException;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class BuilderHttpsConnector extends BuilderHttpConnector {

    private SslContextFactory sslContextFactory;

    public BuilderHttpsConnector(int port) {
        super(port);
    }

    /**
     * Support format: p12
     * @param keyStorePath
     * @return
     */
    public BuilderHttpsConnector withSslContext(String keyStorePath) {
        sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keyStorePath);
        return this;
    }

    /**
     * Support format: p12
     * @param keyStorePath
     * @return
     */
    public BuilderHttpsConnector withSslContext(String keyStorePath, String keyStorePassword) {
        sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        return this;
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

        return connector;
    }
}
