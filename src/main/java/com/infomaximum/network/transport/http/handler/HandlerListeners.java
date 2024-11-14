package com.infomaximum.network.transport.http.handler;

import com.infomaximum.network.event.HttpChannelListener;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.EventsHandler;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class HandlerListeners extends EventsHandler {

    private final Set<HttpChannelListener> listeners;
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public HandlerListeners(Set<HttpChannelListener> httpChannelListeners, Thread.UncaughtExceptionHandler uncaughtExceptionHandler, Handler handler) {
        super(handler);
        this.listeners = new HashSet<>(httpChannelListeners);
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    protected void onBeforeHandling(Request request) {
        for (HttpChannelListener listener : listeners) {
            try {
                listener.onBeforeHandling(request);
            } catch (Throwable thr) {
                uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
            }
        }
    }

    @Override
    protected void onRequestRead(Request request, Content.Chunk chunk) {
        for (HttpChannelListener listener : listeners) {
            try {
                listener.onRequestRead(request, chunk);
            } catch (Throwable thr) {
                uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
            }
        }
    }

    @Override
    protected void onAfterHandling(Request request, boolean handled, Throwable failure) {
        for (HttpChannelListener listener : listeners) {
            try {
                listener.onAfterHandling(request, handled, failure);
            } catch (Throwable thr) {
                uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
            }
        }
    }

    @Override
    protected void onResponseBegin(Request request, int status, HttpFields headers) {
        for (HttpChannelListener listener : listeners) {
            try {
                listener.onResponseBegin(request, status, headers);
            } catch (Throwable thr) {
                uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
            }
        }
    }

    @Override
    protected void onResponseWrite(Request request, boolean last, ByteBuffer content) {
        for (HttpChannelListener listener : listeners) {
            try {
                listener.onResponseWrite(request, last, content);
            } catch (Throwable thr) {
                uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
            }
        }
    }

    @Override
    protected void onResponseWriteComplete(Request request, Throwable failure) {
        for (HttpChannelListener listener : listeners) {
            try {
                listener.onResponseWriteComplete(request, failure);
            } catch (Throwable thr) {
                uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
            }
        }
    }

    @Override
    protected void onResponseTrailersComplete(Request request, HttpFields trailers) {
        for (HttpChannelListener listener : listeners) {
            try {
                listener.onResponseTrailersComplete(request, trailers);
            } catch (Throwable thr) {
                uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
            }
        }
    }


    @Override
    protected void onComplete(Request request, int status, HttpFields headers, Throwable failure) {
        for (HttpChannelListener listener : listeners) {
            try {
                listener.onComplete(request, status, headers, failure);
            } catch (Throwable thr) {
                uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), thr);
            }
        }
    }

}
