package com.infomaximum.network.external.handshake;

import com.infomaximum.network.Session;
import com.infomaximum.network.external.IExecutePacket;

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
}
