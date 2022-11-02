package com.infomaximum.network.protocol;

import com.infomaximum.network.exception.NetworkException;
import com.infomaximum.network.packet.IPacket;
import com.infomaximum.network.session.Session;

import java.util.concurrent.CompletableFuture;

/**
 * Created by kris on 26.08.16.
 */
public interface PacketHandler {

    CompletableFuture<IPacket[]> exec(Session session, IPacket packet);

    abstract class Builder {

        public abstract PacketHandler build(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) throws NetworkException;

    }
}
