package com.infomaximum.network.handler.handshake;

import com.infomaximum.network.handler.PacketHandler;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.session.Session;

/**
 * Created by kris on 01.09.16.
 */
public abstract class Handshake implements PacketHandler {

    public abstract void onPhaseHandshake(Session session);

    /**
     * Завершаем фазу рукопожатия
     * @param session
     */
    public void completedPhaseHandshake(Session session){
        session.getTransportSession().completedPhaseHandshake();
    }

    /**
     * Ошибка фазы рукопожатия - разрываем соединение
     * @param session
     */
    public void failPhaseHandshake(Session session, ResponsePacket responsePacket){
        session.getTransportSession().failPhaseHandshake(responsePacket);
    }
}
