package com.infomaximum.network.handler;

import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.packet.TargetPacket;
import com.infomaximum.network.session.Session;

import java.util.concurrent.CompletableFuture;

/**
 * Created by kris on 26.08.16.
 */
public interface PacketHandler {

    CompletableFuture<ResponsePacket> exec(Session session, TargetPacket packet);

    abstract class Builder {

        public abstract PacketHandler build(NetworkImpl network) throws NetworkException;

    }
}
