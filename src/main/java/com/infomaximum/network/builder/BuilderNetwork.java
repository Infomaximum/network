package com.infomaximum.network.builder;

import com.infomaximum.network.struct.ICodeResponse;
import com.infomaximum.network.struct.ISessionData;
import com.infomaximum.network.Network;
import com.infomaximum.network.external.IExecutePacket;
import com.infomaximum.network.ManagerSession;
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

    private Handshake handshake=null;
    private ICodeResponse codeResponse=null;
    private IExecutePacket executePacket=null;
    private Class<? extends ISessionData> sessionDataClass=null;

    private Class<? extends RequestPacket> extensionRequestPacket=null;


    private Collection<BuilderTransport> builderTransports=null;
    private ManagerSession managerSession=null;

    public BuilderNetwork() {}

    public BuilderNetwork withHandshake(Handshake handshake) {
        this.handshake=handshake;
        return this;
    }

    public BuilderNetwork withCodeResponse(ICodeResponse codeResponse) {
        this.codeResponse=codeResponse;
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

    public BuilderNetwork withManagerSession(ManagerSession managerSession){
        this.managerSession = managerSession;
        return this;
    }

    public BuilderNetwork withSessionData(Class<? extends ISessionData> sessionDataClass){
        this.sessionDataClass=sessionDataClass;
        return this;
    }

    public BuilderNetwork withExtensionRequestPacket(Class<? extends RequestPacket> extensionRequestPacket){
        this.extensionRequestPacket=extensionRequestPacket;
        return this;
    }

    public Network build() throws Exception {
        Network network = new Network(
                managerSession,
                handshake, codeResponse,
                sessionDataClass,
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
