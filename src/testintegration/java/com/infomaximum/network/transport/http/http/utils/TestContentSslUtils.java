package com.infomaximum.network.transport.http.http.utils;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Assert;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.*;

public class TestContentSslUtils {

    public static void testContent(int port, String path, String expectedBody, String protocol) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        testContent(port, path, expectedBody, protocol, null);
    }

    public static void testContent(int port, String path, String expectedBody, String protocol, KeyStore keyStore) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        String body = getContent(port, path, keyStore, protocol);
        Assert.assertEquals(expectedBody, body);
    }

    public static void testConnectionFail(int port, String path, String protocol, KeyStore keyStore) throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        getContent(port, path, keyStore, protocol);
    }

    private static String getContent(int port, String path, KeyStore keyStore, String protocol) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContext sc = SSLContext.getInstance(protocol);
        sc.init(null, getTrustManager(keyStore), new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create and install all-trusting host name verifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://localhost:" + port + path).openConnection();
        Assert.assertEquals(200, connection.getResponseCode());
        String content = getContent(connection);

        connection.disconnect();
        return content;
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
