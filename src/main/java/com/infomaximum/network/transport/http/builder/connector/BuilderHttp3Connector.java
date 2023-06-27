package com.infomaximum.network.transport.http.builder.connector;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.struct.info.HttpConnectorInfo;
import com.infomaximum.network.utils.CertificateUtils;
import com.infomaximum.network.utils.TempKeyStore;
import org.eclipse.jetty.http3.server.HTTP3ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnector;
import org.eclipse.jetty.http3.server.RawHTTP3ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Supplier;

public class BuilderHttp3Connector extends BuilderHttpConnector {

    private KeyStore keyStore;

    //Из-за внутренних особенносте jetty - не принимаются приватные ключи без пароля, потом этот кода - обертку надо удалить
    private TempKeyStore tempKeyStore;

    public BuilderHttp3Connector(int port) {
        super(port);
    }

    public BuilderHttp3Connector withSsl(byte[] certChain, byte[] privateKey) {
        //Как jetty поддержит незашированные приватные ключи перейти на этот механизм - а старый код подчистить
        //keyStore = CertificateUtils.buildKeyStore(certChain, privateKey);
        tempKeyStore = new TempKeyStore(certChain, privateKey);
        return this;
    }

    @Override
    public Connector build(Server server) throws NetworkException {

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);
        httpConfig.addCustomizer(new SecureRequestCustomizer());
        if (requestHeaderSize != null) {
            httpConfig.setRequestHeaderSize(requestHeaderSize);
        }
        if (responseHeaderSize != null) {
            httpConfig.setResponseHeaderSize(responseHeaderSize);
        }
        HTTP3ServerConnectionFactory http3ConnectionFactory = new HTTP3ServerConnectionFactory(httpConfig);


        if (keyStore == null && tempKeyStore == null) {
            throw new RuntimeException("Not set ssl context");
        }

        SslContextFactory.Server sslContextFactory = tempKeyStore.sslContextFactory;
        //Как jetty поддержит незашированные приватные ключи перейти на этот механизм - а старый код подчистить
//        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
//        sslContextFactory.setKeyStore(keyStore);


        HTTP3ServerConnector connector = new HTTP3ServerConnector(server, sslContextFactory, http3ConnectionFactory);
        connector.setPort(port);
        connector.setHost(host);

        return connector;
    }

    public Supplier<? extends HttpConnectorInfo> getInfoSupplier() {
        return () -> new HttpConnectorInfo(host, port);
    }

}
