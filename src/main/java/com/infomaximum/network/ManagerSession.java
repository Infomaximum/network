package com.infomaximum.network;

import com.infomaximum.network.packet.ResponsePacket;
import com.infomaximum.network.session.Session;
import net.minidev.json.JSONObject;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ManagerSession {

    Session getSession(String uuid);

    void sendAsyncIfConnect(Serializable user, String controller, String method, JSONObject data);

    ResponsePacket sendRequest(Serializable user, String controller, String method, JSONObject data) throws TimeoutException, ExecutionException, InterruptedException;

    ResponsePacket sendRequest(Serializable user, String controller, String method, JSONObject data, long timeout) throws TimeoutException, ExecutionException, InterruptedException;

}
