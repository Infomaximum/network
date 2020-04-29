package com.infomaximum.network.transport.http.http.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

import java.io.IOException;
import java.net.URI;

/**
 * Created by kris on 13.06.17.
 */
public class TestContentUtils {

    public static String getContent(int port, String path) throws IOException {
        HttpGet httpGet = new HttpGet(URI.create("http://localhost:" + port + path));
        return getContent(httpGet);
    }

    public static String getContent(HttpGet httpGet) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = client.execute(httpGet)) {

                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());

                Assert.assertEquals(200, statusCode);

                return body;
            }
        }
    }

    public static int getStatusCode(int port, String path) throws IOException {
        HttpGet httpGet = new HttpGet(URI.create("http://localhost:" + port + path));
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                return response.getStatusLine().getStatusCode();
            }
        }
    }


    public static void testContent(int port, String path, String expectedBody) throws IOException {
        String body = getContent(port, path);
        Assert.assertEquals(expectedBody, body);
    }
}
