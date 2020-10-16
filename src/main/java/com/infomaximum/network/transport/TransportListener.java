package com.infomaximum.network.transport;

import net.minidev.json.JSONObject;

/**
 * Created by kris on 26.08.16.
 */
public interface TransportListener {

    public void onConnect(Transport transport, Object channel, String remoteIpAddress);

    public void incomingPacket(Transport transport, Object channel, JSONObject jPacket);

    public void onDisconnect(Transport transport, Object channel, int statusCode, Throwable throwable);
}
