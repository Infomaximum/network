package com.infomaximum.network.packet;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;

/**
 * Created by kris on 25.08.16.
 */
public abstract class Packet {

    private final JSONObject data;

    protected Packet(JSONObject data){
        this.data=data;
    }

    public abstract TypePacket getType();

    public JSONObject getData() {
        return data;
    }

    public String serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType().getId());
        serializeNative(jsonObject);
        if (data!=null) jsonObject.put("data", data);
        return jsonObject.toJSONString();
    }

    protected abstract void serializeNative(JSONObject jsonObject);
}
