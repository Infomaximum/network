package com.infomaximum.network.builder;

import com.infomaximum.network.SessionDataBuilder;
import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.session.manager.MultiManagerSession;
import com.infomaximum.network.Network;
import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.session.manager.ManagerSessionImpl;
import com.infomaximum.network.external.handshake.Handshake;
import com.infomaximum.network.packet.RequestPacket;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.http.HttpTransport;
import com.infomaximum.network.transport.http.builder.HttpBuilderTransport;
import com.infomaximum.network.transport.socket.SocketTransport;
import com.infomaximum.network.transport.socket.builder.SocketBuilderTransport;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by kris on 26.08.16.
 */
public class BuilderNetwork {

    public enum TypeSessionManager{
        SINGLE, MULTI
    }

    private Handshake handshake = null;
    private IExecutePacket executePacket = null;

    private SessionDataBuilder sessionDataBuilder = null;

    private Class<? extends RequestPacket> extensionRequestPacket = null;

    private Collection<BuilderTransport> builderTransports = null;
    private TypeSessionManager typeSessionManager = null;

    public BuilderNetwork() {}

    public BuilderNetwork withHandshake(Handshake handshake) {
        this.handshake=handshake;
        return this;
    }

    public BuilderNetwork withExecutePacket(IExecutePacket executePacket) {
        this.executePacket=executePacket;
        return this;
    }

    public BuilderNetwork withTransport(BuilderTransport builderTransport) {
        if (builderTransports==null) builderTransports = new HashSet<BuilderTransport>();
        builderTransports.add(builderTransport);
        return this;
    }

    public BuilderNetwork withManagerSession(TypeSessionManager type){
        this.typeSessionManager = type;
        return this;
    }

    public BuilderNetwork withSessionDataBuilder(SessionDataBuilder sessionDataBuilder){
        this.sessionDataBuilder = sessionDataBuilder;
        return this;
    }

    public BuilderNetwork withExtensionRequestPacket(Class<? extends RequestPacket> extensionRequestPacket){
        this.extensionRequestPacket=extensionRequestPacket;
        return this;
    }

    public Network build() throws Exception {
        ManagerSessionImpl managerSession = null;
        if (typeSessionManager!=null) {
            switch (typeSessionManager) {
                case SINGLE:
                    throw new RuntimeException("Not implemented");
                case MULTI:
                    managerSession = new MultiManagerSession();
                    break;
            }
        }

        NetworkImpl network = new NetworkImpl(
                managerSession,
                handshake,
                sessionDataBuilder,
                extensionRequestPacket,
                executePacket
        );

        if (builderTransports!=null) {
            for (BuilderTransport builderTransport: builderTransports) {
                Transport transport;
                if (builderTransport instanceof HttpBuilderTransport) {
                    transport = new HttpTransport((HttpBuilderTransport) builderTransport);
                } else if (builderTransport instanceof SocketBuilderTransport) {
                    transport = new SocketTransport((SocketBuilderTransport) builderTransport);
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
