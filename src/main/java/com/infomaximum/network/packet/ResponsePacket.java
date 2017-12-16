package com.infomaximum.network.packet;

import net.minidev.json.JSONAware;
import net.minidev.json.JSONObject;

import java.io.Serializable;

/**
 * Created by kris on 26.08.16.
 */
public class ResponsePacket extends Packet implements IPacketId {

    private final long id;

    //Опциональное поле, для пакетов типа RESPONSE
    private JSONAware dataException;

    public ResponsePacket(long id, JSONObject data, JSONAware dataException) {
        super(data);
        this.id = id;
        this.dataException = dataException;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public TypePacket getType() {
        return TypePacket.RESPONSE;
    }

    @Override
    protected void serializeNative(JSONObject jsonObject) {
        jsonObject.put("id", id);
        if (dataException==null) {
            jsonObject.put("error", dataException);
        }
    }

    public static ResponsePacket responseAccept(IPacketId request, JSONObject data) {
        return new ResponsePacket(request.getId(), data, null);
    }

    public static ResponsePacket responseException(IPacketId request, JSONAware dataException) {
        return new ResponsePacket(request.getId(), null, dataException);
    }
}
