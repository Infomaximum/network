package com.infomaximum.network.transport;

/**
 * Created by kris on 26.08.16.
 */
public interface TransportListener {

    public void onConnect(Transport transport, Object channel, String remoteIpAddress);

    public void incomingMessage(Transport transport, Object channel, String message);

    public void onDisconnect(Transport transport, Object channel, int statusCode, Throwable throwable);
}
