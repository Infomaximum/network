package com.infomaximum.network;

import com.infomaximum.network.event.NetworkListener;
import com.infomaximum.network.packet.ResponsePacket;
import net.minidev.json.JSONObject;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by kris on 30.08.16.
 */
public abstract class ManagerSession implements NetworkListener {

    @Override
    public void onConnect(Session session) {}//Сетевые события игнорируем нам необходимы авторизационные

    @Override
    public void onHandshake(Session session) {}//Сетевые события игнорируем нам необходимы авторизационные

    @Override
    public void onDisconnect(Session session) {}//Сетевые события игнорируем нам необходимы авторизационные

    public abstract void sendAsyncIfConnect(Serializable user, String controller, String method, JSONObject data);

    public abstract ResponsePacket sendRequest(Serializable user, String controller, String method, JSONObject data) throws TimeoutException, ExecutionException, InterruptedException;

    public abstract ResponsePacket sendRequest(Serializable user, String controller, String method, JSONObject data, long timeout) throws TimeoutException, ExecutionException, InterruptedException;

    public abstract void destroyed();
}
