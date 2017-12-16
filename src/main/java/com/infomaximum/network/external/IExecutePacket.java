package com.infomaximum.network.external;

import com.infomaximum.network.Session;
import com.infomaximum.network.exception.ResponseException;
import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.packet.TargetPacket;
import net.minidev.json.JSONObject;

import java.util.concurrent.CompletableFuture;

/**
 * Created by kris on 26.08.16.
 */
public interface IExecutePacket {

    public CompletableFuture<ResponsePacket> exec(Session session, TargetPacket packet) throws ResponseException;

}
