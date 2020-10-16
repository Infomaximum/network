package com.infomaximum.network.protocol.standard.handler.handshake;

import com.infomaximum.network.protocol.standard.handler.PacketHandler;
import com.infomaximum.network.protocol.standard.packet.ResponsePacket;
import com.infomaximum.network.session.Session;
import com.infomaximum.network.struct.HandshakeData;

/**
 * Created by kris on 01.09.16.
 */
public abstract class Handshake implements PacketHandler {

    public abstract void onPhaseHandshake(Session session);

    /**
     * Завершаем фазу рукопожатия
     * @param session
     */
    public void completedPhaseHandshake(Session session, HandshakeData handshakeData) {
        session.getTransportSession().completedPhaseHandshake(handshakeData);
    }

    /**
     * Ошибка фазы рукопожатия - разрываем соединение
     * @param session
     */
    public void failPhaseHandshake(Session session, ResponsePacket responsePacket){
        session.getTransportSession().failPhaseHandshake(responsePacket);
    }
}
