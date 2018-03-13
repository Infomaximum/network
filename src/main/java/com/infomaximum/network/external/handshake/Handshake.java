package com.infomaximum.network.external.handshake;

import com.infomaximum.network.session.Session;
import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.packet.ResponsePacket;

/**
 * Created by kris on 01.09.16.
 */
public abstract class Handshake implements IExecutePacket {

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
