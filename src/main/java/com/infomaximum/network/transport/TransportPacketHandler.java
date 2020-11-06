package com.infomaximum.network.transport;

import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.protocol.PacketHandler;
import com.infomaximum.network.session.Session;

public interface TransportPacketHandler {

    boolean isPhaseHandshake();

    PacketHandler getPacketHandler();

    Session getSession();

    IPacket parse(String message) throws Exception;

    void send(IPacket packet);

}
