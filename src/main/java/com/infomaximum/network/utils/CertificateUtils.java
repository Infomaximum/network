package com.infomaximum.network.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class CertificateUtils {

    public static Certificate[] builds(byte[] certChain) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        try (InputStream in = new ByteArrayInputStream(certChain)) {
            return certificateFactory.generateCertificates(in).stream().toArray(Certificate[]::new);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyStore buildKeyStore(byte[] certChain, byte[] privKey) {
        //Создаем KeyStore
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, new char[0]);

            PrivateKey privateKey = buildPrivateKeyFromPEM(privKey);
            Certificate[] certificateChain = builds(certChain);

            keyStore.setKeyEntry("ext-default", privateKey, null, certificateChain);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        return keyStore;
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
