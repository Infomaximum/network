package com.infomaximum.network.external;

import com.infomaximum.network.Session;
import com.infomaximum.network.exception.ResponseException;
import com.infomaximum.network.packet.TargetPacket;
import net.minidev.json.JSONObject;

/**
 * Created by kris on 26.08.16.
 */
public interface IExecutePacket {

    public JSONObject exec(Session session, TargetPacket packet) throws ResponseException;

}
