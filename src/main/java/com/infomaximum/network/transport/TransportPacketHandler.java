package com.infomaximum.network.transport;

import com.infomaximum.network.exception.ParsePacketNetworkException;
import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.PacketHandler;
import com.infomaximum.network.session.Session;

public interface TransportPacketHandler {

    boolean isPhaseHandshake();

    PacketHandler getPacketHandler();

    Session getSession();

    IPacket parse(String message) throws ParsePacketNetworkException;

    void send(IPacket packet);

    Thread.UncaughtExceptionHandler getUncaughtExceptionHandler();
}
