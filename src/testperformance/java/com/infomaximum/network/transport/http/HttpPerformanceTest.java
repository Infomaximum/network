package com.infomaximum.network.transport.http;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpPerformanceTest {

    private final static Logger log = LoggerFactory.getLogger(HttpPerformanceTest.class);

    private static final int port = 8099;

    private Network network;

    @BeforeAll
    public void init() throws Exception {
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(new BuilderHttpConnector(port))
                )
                .build();
    }


    @Test
    public void staticFileTest() throws Exception {
//        Thread.sleep(1000000000L);

//        HttpGet httpGet = new HttpGet(URI.create("http://localhost:" + port + "/static/internal.1.txt"));

//        ExecutorService executors = Executors.newCachedThreadPool();

//        int count = 100;
//        while (count-- > 0) {
//            long t1 = System.currentTimeMillis();
//
//            int threads = 1222;
//            List<CompletableFuture<String>> taskList = new ArrayList<>();
//            for (int i = 0; i < threads; i++) {
//                CompletableFuture<String> responseFuture = new CompletableFuture<>();
//
//                taskList.add(responseFuture);
//                executors.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            String body = TestContentUtils.getContent(httpGet);
//                            Assert.assertEquals("/webapp/static/internal.1.txt", body);
//                            responseFuture.complete(body);
//                        } catch (Throwable e) {
//                            responseFuture.completeExceptionally(e);
//                        }
//                    }
//                });
//
//            }
//
//            //Ожидем завершения всех потоков
//            int countSuccess = 0;
//            for (CompletableFuture<String> futureTask : taskList) {
//                futureTask.join();
//                countSuccess++;
//            }
//            Assert.assertEquals(countSuccess, taskList.size());
//
//            long t2 = System.currentTimeMillis();
//
//            log.error("Complete, time: {}", (t2 - t1));
//            Thread.sleep(1000L);
//        }
    }

    @AfterAll
    public void destroy() throws Exception {
        network.close();
        network = null;
    }
}
