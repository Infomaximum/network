package com.infomaximum.network.event;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;

import java.nio.ByteBuffer;

/**
 * Обертка над:
 * org.eclipse.jetty.server.handler.EventsHandler
 */
public interface HttpChannelListener {

    default void onBeforeHandling(Request request) {}

    default void onRequestRead(Request request, Content.Chunk chunk) {}

    default void onAfterHandling(Request request, boolean handled, Throwable failure) {}

    default void onResponseBegin(Request request, int status, HttpFields headers) { }

    default void onResponseWrite(Request request, boolean last, ByteBuffer content) {}

    default void onResponseWriteComplete(Request request, Throwable failure) {}

    default  void onResponseTrailersComplete(Request request, HttpFields trailers) {}

    default void onComplete(Request request, int status, HttpFields headers, Throwable failure) {}

}
