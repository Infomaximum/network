package com.infomaximum.network.protocol.standard.packet;

import com.infomaximum.network.packet.IPacket;
import net.minidev.json.JSONObject;

/**
 * Created by kris on 26.08.16.
 */
public class ResponsePacket extends Packet implements IPacketId {

    private final long id;
    private final int code;

    public ResponsePacket(long id, int code, JSONObject data) {
        super(data);
        this.id = id;
        this.code = code;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public TypePacket getType() {
        return TypePacket.RESPONSE;
    }

    public int getCode() {
        return code;
    }

    @Override
    protected void serializeNative(JSONObject jsonObject) {
        jsonObject.put("id", id);
        jsonObject.put("code", code);
    }

    public static ResponsePacket build(IPacketId request, int code, JSONObject data) {
        return new ResponsePacket(request.getId(), code, data);
    }

    public static IPacket[] response(IPacketId request, int code, JSONObject data) {
        return new IPacket[]{ build(request, code, data) };
    }
}
