package com.infomaximum.network.transport.http.http.utils;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Assertions;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;

public class TestContentSslUtils {

    public static void testContent(int port, String path, String expectedBody, String protocol, KeyStore keyStore) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        String body = getContent(port, path, keyStore, protocol);
        Assertions.assertEquals(expectedBody, body);
    }

    public static void testConnectionFail(int port, String path, String protocol, KeyStore keyStore) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        getContent(port, path, keyStore, protocol);
    }

    public static void testContentTwoWaySslAuthorization(int port, String path, String expectedBody, Path clientKeyStorePath, Path clientTrustStorePath, String protocol, String password) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException {
        String body = getContentTwoWaySslAuthorization(port, path, clientKeyStorePath, clientTrustStorePath, protocol, password);
        Assertions.assertEquals(expectedBody, body);
    }

    private static String getContentTwoWaySslAuthorization(int port, String path, Path clientKeyStorePath, Path clientTrustStorePath, String protocol, String password) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(new FileInputStream(clientKeyStorePath.toFile()), password.toCharArray());
        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        truststore.load(new FileInputStream(clientTrustStorePath.toFile()), password.toCharArray());

        SSLContext sc = SSLContext.getInstance(protocol);
        sc.init(getKeyManagers(keystore, password), getTrustManager(truststore), new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create and install all-trusting host name verifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://localhost:" + port + path).openConnection();
        Assertions.assertEquals(200, connection.getResponseCode());
        String content = getContent(connection);

        connection.disconnect();
        return content;
    }

    private static String getContent(int port, String path, KeyStore keyStore, String protocol) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContext sc = SSLContext.getInstance(protocol);
        sc.init(null, getTrustManager(keyStore), new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create and install all-trusting host name verifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://localhost:" + port + path).openConnection();
        Assertions.assertEquals(200, connection.getResponseCode());
        String content = getContent(connection);

        connection.disconnect();
        return content;
    }

    private static KeyManager[] getKeyManagers(KeyStore keyStore, String password) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password.toCharArray());
        return kmf.getKeyManagers();
    }

    private static String getContent(HttpsURLConnection connection) throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return br.readLine();
        }
    }

    private static TrustManager[] getTrustManager(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {
        if (keyStore != null) {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keyStore);
            TrustManager[] tms = factory.getTrustManagers();
            if (tms.length == 0) {
                throw new NoSuchAlgorithmException("Unable to load keystore");
            }
            return tms;
        } else {
            return SslContextFactory.TRUST_ALL_CERTS;
        }
    }
}
