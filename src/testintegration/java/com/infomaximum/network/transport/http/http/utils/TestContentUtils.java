package com.infomaximum.network.transport.http.http.utils;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Created by kris on 13.06.17.
 */
public class TestContentUtils {

    public static String getContent(int port, String path) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .build();
        HttpResponse body = client.send(request, HttpResponse.BodyHandlers.ofString());
        return (String) body.body();
    }

    public static byte[] getContentBytes(int port, String path) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .build();
        HttpResponse body = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return (byte[]) body.body();
    }

    public static int getStatusCode(int port, String path) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .build();
        HttpResponse body = client.send(request, HttpResponse.BodyHandlers.ofString());
        return body.statusCode();
    }


    public static void testContent(int port, String path, String expectedBody) throws IOException, InterruptedException {
        String body = getContent(port, path);
        Assertions.assertEquals(expectedBody, body);
    }
}
