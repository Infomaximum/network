package com.infomaximum.network.transport;

import com.infomaximum.network.packet.Packet;

/**
 * Created by kris on 26.08.16.
 */
public interface TransportListener {

    public void onConnect(Transport transport, Object channel, String remoteIpAddress);

    public void incomingPacket(Transport transport, Object channel, Packet packet);

    public void onDisconnect(Transport transport, Object channel, int statusCode, Throwable throwable);
}
