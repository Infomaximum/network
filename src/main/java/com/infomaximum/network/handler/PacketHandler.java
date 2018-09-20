package com.infomaximum.network.handler;

import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.packet.TargetPacket;
import com.infomaximum.network.session.Session;

import java.util.concurrent.CompletableFuture;

/**
 * Created by kris on 26.08.16.
 */
public interface PacketHandler {

    public CompletableFuture<ResponsePacket> exec(Session session, TargetPacket packet);

    public abstract static class Builder {

        public abstract PacketHandler build(NetworkImpl network) throws ReflectiveOperationException;

    }
}
