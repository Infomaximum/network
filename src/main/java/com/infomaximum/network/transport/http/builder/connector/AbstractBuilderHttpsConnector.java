package com.infomaximum.network.transport.http.builder.connector;

import com.infomaximum.network.exception.NetworkException;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

abstract class AbstractBuilderHttpsConnector extends BuilderHttpConnector {

    public AbstractBuilderHttpsConnector(int port) {
        super(port);
    }

    protected abstract SslContextFactory.Server getSslContextFactory();

    @Override
    public Connector build(Server server) throws NetworkException {
        SslContextFactory.Server sslContextFactory = getSslContextFactory();
        if (sslContextFactory == null) throw new NetworkException("Not init ssl key");

        HttpConfiguration httpsConfig = createHttpConfiguration();
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

        return connector;
    }
}
