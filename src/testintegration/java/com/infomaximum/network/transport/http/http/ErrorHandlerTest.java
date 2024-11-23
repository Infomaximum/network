package com.infomaximum.network.transport.http.http;

import com.infomaximum.network.Network;
import com.infomaximum.network.builder.BuilderNetwork;
import com.infomaximum.network.transport.http.SpringConfigurationMvc;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.http.builder.connector.BuilderHttpConnector;
import com.infomaximum.network.transport.http.http.utils.TestContentUtils;
import jakarta.servlet.ServletException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ErrorHandlerTest {

    private static final int port = 8099;

    private static TestErrorHandler testErrorHandler;
    private static Network network;

    @BeforeAll
    public static void init() throws Exception {
        testErrorHandler = new TestErrorHandler();
        network = new BuilderNetwork()
                .withTransport(
                        new HttpBuilderTransport(SpringConfigurationMvc.class)
                                .addConnector(new BuilderHttpConnector(port))
                                .withErrorHandler(testErrorHandler)
                )
                .build();
    }


    @Test
    public void testFail() throws Exception {
        testErrorHandler.reset();

        String expectedErrorMessage = "TestException";

        TestContentUtils.getContent(port, "/test/testException?message=" + expectedErrorMessage);

        Throwable throwable = testErrorHandler.getThrowable();
        Assertions.assertNotNull(throwable);
        Assertions.assertEquals(ServletException.class, throwable.getClass());

        Throwable causeThrowable = throwable.getCause();
        Assertions.assertEquals(RuntimeException.class, causeThrowable.getClass());
        Assertions.assertEquals(expectedErrorMessage, causeThrowable.getMessage());
    }

    @AfterAll
    public static void destroy() throws Exception {
        network.close();
        network = null;
    }

    private static class TestErrorHandler implements com.infomaximum.network.struct.ErrorHandler {

        private Throwable throwable;

        @Override
        public void handle(Request request, Response response, Throwable throwable) {
            if (this.throwable != null) {
                Assertions.fail();
                throw new RuntimeException();
            }
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void reset() {
            this.throwable = null;
        }
    }
}
