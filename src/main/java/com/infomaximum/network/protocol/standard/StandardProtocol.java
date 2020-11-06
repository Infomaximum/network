package com.infomaximum.network.protocol.standard;

import com.infomaximum.network.protocol.PacketHandler;
import com.infomaximum.network.protocol.Protocol;
import com.infomaximum.network.protocol.standard.handler.handshake.Handshake;
import com.infomaximum.network.protocol.standard.session.StandardTransportSession;
import com.infomaximum.network.session.Session;
import com.infomaximum.network.transport.Transport;

public class StandardProtocol extends Protocol {

    private final Handshake handshake;

    private final PacketHandler packetHandler;

    protected StandardProtocol(
            Handshake handshake,
            PacketHandler packetHandler,
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler
    ) {
        super(uncaughtExceptionHandler);

        this.handshake = handshake;
        this.packetHandler = packetHandler;
    }

    @Override
    public String getName() {
        return "ws-mvc";
    }

    @Override
    public StandardTransportSession onConnect(Transport transport, Object channel) throws Exception {

        StandardTransportSession transportSession = new StandardTransportSession(this, transport, channel);

        //Начинаем фазу рукопожатия
        if (handshake == null) {
            //Прикольно у нас нет обработчика рукопожатий, сразу считаем что оно свершилось)
            onHandshake(transportSession.getSession());
        } else {
            handshake.onPhaseHandshake(transportSession.getSession());
        }

        return transportSession;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void onHandshake(Session session) {
        //Оповещаем подписчиков
//        for (NetworkListener listener : listeners) {
//            listener.onHandshake(session);
//        }
    }

}
