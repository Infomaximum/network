package com.infomaximum.network.packet;

import net.minidev.json.JSONObject;

/**
 * Created by kris on 31.08.16.
 */
public abstract class TargetPacket extends Packet {

    //Опциональное поле, для пакетов типа REQUEST или ASYNC
    public final String controller;

    //Опциональное поле, для пакетов типа REQUEST или ASYNC
    public final String action;

    public TargetPacket(String controller, String action, JSONObject data) {
        super(data);
        this.controller = controller;
        this.action = action;
    }

    @Override
    protected void serializeNative(JSONObject jsonObject) {
        jsonObject.put("controller", controller);
        jsonObject.put("action", action);
    }
}
