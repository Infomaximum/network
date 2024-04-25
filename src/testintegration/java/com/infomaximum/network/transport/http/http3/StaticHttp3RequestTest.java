package com.infomaximum.network.transport.http.http3;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttp3Connector;
import com.infomaximum.network.transport.http.http.utils.TestContentUtils;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http3.HTTP3Configuration;
import org.eclipse.jetty.http3.api.Session;
import org.eclipse.jetty.http3.api.Stream;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.frames.DataFrame;
import org.eclipse.jetty.http3.frames.HeadersFrame;
import org.eclipse.jetty.quic.common.QuicConfiguration;
import org.eclipse.jetty.util.Jetty;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by kris on 26.08.16.
 *
 * Тест проверяющий, что на запрос приходит ответ
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StaticHttp3RequestTest {

    private static final int port = 8099;

    private Network network;

    @BeforeAll
    public void init() throws Exception {
        BuilderHttp3Connector builderHttp3Connector = new BuilderHttp3Connector(port)
                .withSsl(
                        this.getClass().getClassLoader().getResourceAsStream("http3test/localhost.crt").readAllBytes(),
                        this.getClass().getClassLoader().getResourceAsStream("http3test/localhost.key").readAllBytes()
                );
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(builderHttp3Connector)
                )
                .build();

        Thread.sleep(1000L);
    }


    @Test
    public void staticFileTest1() throws Exception {
        String result = request(port, "/static/internal.1.txt");
        Assertions.assertEquals("/webapp/static/internal.1.txt", result);
    }

    @Test
    public void staticFileTest2() throws Exception {
        String result = request(port, "/static/1/internal.2.txt");
        Assertions.assertEquals("webapp/static/1/internal.2.txt", result);
    }

    private static String request(int port, String path) throws Exception {
        HTTP3Client client = new HTTP3Client();

        QuicConfiguration quicConfig = client.getQuicConfiguration();
//        quicConfig.setVerifyPeerCertificates(false);

        HTTP3Configuration h3Config = client.getHTTP3Configuration();

        client.start();


        String host = "localhost";
        Session.Client session = client
                .connect(new InetSocketAddress(host, port), new Session.Client.Listener() {})
                .get(15, TimeUnit.SECONDS);


        // Prepare the HTTP request object.
        MetaData.Request request = new MetaData.Request("GET", HttpURI.from("https://" + host + ":" + port + path), HttpVersion.HTTP_3, HttpFields.build());

        HeadersFrame headersFrame = new HeadersFrame(request, true);

        CompletableFuture<byte[]> resultFuture = new CompletableFuture<>();
        Stream stream = session.newRequest(headersFrame, new Stream.Client.Listener() {

            private ByteArrayOutputStream os = new ByteArrayOutputStream();

            @Override
            public void onResponse(Stream.Client stream, HeadersFrame frame) {
                MetaData metaData = frame.getMetaData();
                MetaData.Response response = (MetaData.Response)metaData;

                System.out.println("response= " + response);
                stream.demand();
            }

            @Override
            public void onDataAvailable(Stream.Client stream) {
                Stream.Data data = stream.readData();
                if (data == null) {
                    stream.demand();
                } else {
                    ByteBuffer byteBuffer = data.getByteBuffer();
                    while (byteBuffer.hasRemaining()) {
                        os.write(byteBuffer.get());
                    }

                    data.complete();
                    if (!data.isLast()){
                        stream.demand();
                    } else {
                        resultFuture.complete(os.toByteArray());
                    }
                }
            }
        }).get();

        byte[] result = resultFuture.get();
        return new String(result);
    }



    @AfterAll
    public void destroy() throws Exception {
        network.close();
        network=null;
    }
}
