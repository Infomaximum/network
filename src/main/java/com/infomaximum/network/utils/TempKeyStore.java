package com.infomaximum.network.utils;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

public class TempKeyStore {

    private final String password;
    private final KeyStore keyStore;

    public final SslContextFactory.Server sslContextFactory;

    public TempKeyStore(byte[] certChain, byte[] privKey) {
        this.password = UUID.randomUUID().toString();

        //Генерируем keyStore
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, password.toCharArray());

            PrivateKey privateKey = buildPrivateKeyFromPEM(privKey);
            Certificate[] certificateChain = CertificateUtils.builds(certChain);

            keyStore.setKeyEntry("ext-default", privateKey, password.toCharArray(), certificateChain);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                 InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        //Создаем jks-файл
        try {
            Path jksFile = Files.createTempFile(null, ".jks").toAbsolutePath();
            try (OutputStream fos = new FileOutputStream(jksFile.toFile())) {
                keyStore.store(fos, password.toCharArray());
            }
            jksFile.toFile().deleteOnExit();

            sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(jksFile.toString());
            sslContextFactory.setKeyStorePassword(password);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private static RSAPrivateKey buildPrivateKeyFromPEM(byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String key = new String(privateKey, Charset.defaultCharset());
        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
