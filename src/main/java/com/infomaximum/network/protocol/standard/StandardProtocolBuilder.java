package com.infomaximum.network.protocol.standard;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.protocol.Protocol;
import com.infomaximum.network.protocol.ProtocolBuilder;
import com.infomaximum.network.protocol.standard.handler.PacketHandler;
import com.infomaximum.network.protocol.standard.handler.handshake.Handshake;

public class StandardProtocolBuilder extends ProtocolBuilder {

    private Handshake handshake = null;
    private PacketHandler.Builder packetHandlerBuilder = null;

    public StandardProtocolBuilder() {
    }

    public StandardProtocolBuilder withHandshake(Handshake handshake) {
        this.handshake = handshake;
        return this;
    }

    public StandardProtocolBuilder withPacketHandler(PacketHandler.Builder packetHandlerBuilder) {
        this.packetHandlerBuilder = packetHandlerBuilder;
        return this;
    }

    @Override
    public Protocol build(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) throws NetworkException {
        return new StandardProtocol(
                handshake,
                packetHandlerBuilder.build(uncaughtExceptionHandler)
        );
    }
}
