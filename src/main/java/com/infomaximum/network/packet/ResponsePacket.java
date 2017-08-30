package com.infomaximum.network.packet;

import com.infomaximum.network.exception.ResponseException;
import net.minidev.json.JSONObject;

import java.io.Serializable;

/**
 * Created by kris on 26.08.16.
 */
public class ResponsePacket extends Packet implements IPacketId {

    private final long id;

    //Опциональное поле, для пакетов типа RESPONSE
    private final Serializable code;

    public ResponsePacket(long id, Serializable code, JSONObject data) {
        super(data);
        this.id = id;
        this.code=code;
    }

    @Override
    public long getId() {
        return id;
    }

    public Serializable getCode() {
        return code;
    }

    @Override
    public TypePacket getType() {
        return TypePacket.RESPONSE;
    }

    @Override
    protected void serializeNative(JSONObject jsonObject) {
        jsonObject.put("id", id);
        jsonObject.put("code", code);
    }

    public static ResponsePacket response(IPacketId request, Serializable code, JSONObject data) {
        return new ResponsePacket(request.getId(), code, data);
    }
}
