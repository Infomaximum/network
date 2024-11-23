package com.infomaximum.network.transport.http.handler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;

public class HttpErrorHandler extends ErrorHandler {

    private final com.infomaximum.network.struct.ErrorHandler errorHandler;
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public HttpErrorHandler(com.infomaximum.network.struct.ErrorHandler errorHandler, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.errorHandler = errorHandler;
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        try {
            Throwable throwable =  (Throwable) request.getAttribute(ErrorHandler.ERROR_EXCEPTION);;
            errorHandler.handle(request, response, throwable);
        } catch (Throwable thr) {
            uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
        }
        callback.succeeded();//Крайне важный вызов, без него соединение keep-alive зависнет и все последующие запросы от браузера в рамках этого соединения также зависнут
        return true;
    }
}
