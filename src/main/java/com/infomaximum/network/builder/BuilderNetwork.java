package com.infomaximum.network.builder;

import com.infomaximum.network.Network;
import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.protocol.Protocol;
import com.infomaximum.network.protocol.ProtocolBuilder;
import com.infomaximum.network.protocol.standard.packet.RequestPacket;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.http.HttpTransport;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by kris on 26.08.16.
 */
public class BuilderNetwork {

    private Class<? extends RequestPacket> extensionRequestPacket = null;

    private Collection<BuilderTransport> builderTransports = null;

    private Collection<ProtocolBuilder> protocolBuilders = null;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public BuilderNetwork() {
    }

    public BuilderNetwork withTransport(BuilderTransport builderTransport) {
        if (builderTransports == null) builderTransports = new HashSet<BuilderTransport>();
        builderTransports.add(builderTransport);
        return this;
    }

    public BuilderNetwork withProtocol(ProtocolBuilder protocolBuilder) {
        if (protocolBuilders == null) protocolBuilders = new HashSet<ProtocolBuilder>();
        protocolBuilders.add(protocolBuilder);
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

    public Collection<BuilderTransport> getBuilderTransports() {
        return builderTransports;
    }

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    public Network build() throws NetworkException {
        List<Protocol> protocols = new ArrayList<>();
        if (protocolBuilders != null) {
            for (ProtocolBuilder builder : protocolBuilders) {
                protocols.add(builder.build(uncaughtExceptionHandler));
            }
        }

        NetworkImpl network = new NetworkImpl(
                protocols,
                extensionRequestPacket,
                uncaughtExceptionHandler
        );

        if (builderTransports != null) {
            for (BuilderTransport builderTransport : builderTransports) {
                Transport transport;
                if (builderTransport instanceof HttpBuilderTransport) {
                    transport = new HttpTransport((HttpBuilderTransport) builderTransport, uncaughtExceptionHandler);
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
