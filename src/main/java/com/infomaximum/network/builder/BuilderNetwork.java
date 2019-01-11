package com.infomaximum.network.builder;

import com.infomaximum.network.Network;
import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.handler.PacketHandler;
import com.infomaximum.network.handler.handshake.Handshake;
import com.infomaximum.network.packet.RequestPacket;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.http.HttpTransport;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by kris on 26.08.16.
 */
public class BuilderNetwork {

    private Handshake handshake = null;
    private PacketHandler.Builder packetHandlerBuilder = null;

    private Class<? extends RequestPacket> extensionRequestPacket = null;

    private Collection<BuilderTransport> builderTransports = null;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public BuilderNetwork() {
    }

    public BuilderNetwork withHandshake(Handshake handshake) {
        this.handshake = handshake;
        return this;
    }

    public BuilderNetwork withPacketHandler(PacketHandler.Builder packetHandlerBuilder) {
        this.packetHandlerBuilder = packetHandlerBuilder;
        return this;
    }

    public BuilderNetwork withTransport(BuilderTransport builderTransport) {
        if (builderTransports == null) builderTransports = new HashSet<BuilderTransport>();
        builderTransports.add(builderTransport);
        return this;
    }

    public BuilderNetwork withExtensionRequestPacket(Class<? extends RequestPacket> extensionRequestPacket) {
        this.extensionRequestPacket = extensionRequestPacket;
        return this;
    }

    public BuilderNetwork withUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    public Network build() throws Exception {
        NetworkImpl network = new NetworkImpl(
                handshake,
                extensionRequestPacket,
                packetHandlerBuilder,
                uncaughtExceptionHandler
        );

        if (builderTransports != null) {
            for (BuilderTransport builderTransport : builderTransports) {
                Transport transport;
                if (builderTransport instanceof HttpBuilderTransport) {
                    transport = new HttpTransport((HttpBuilderTransport) builderTransport);
                } else {
                    throw new RuntimeException("Nothing type builder transport: " + builderTransport);
                }
                transport.addTransportListener(network);
                network.registerTransport(transport);
            }
        }
        return network;
    }
}
