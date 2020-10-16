package com.infomaximum.network.protocol.standard.packet;

import net.minidev.json.JSONObject;

/**
 * Created by kris on 26.08.16.
 */
public class RequestPacket extends TargetPacket implements IPacketId {

    private final long id;

    public RequestPacket(long id, String controller, String action, JSONObject data) {
        super(controller, action, data);
        this.id=id;
    }

    public RequestPacket(JSONObject parse) {
        super(
                parse.getAsString("controller"),
                parse.getAsString("action"),
                (JSONObject) parse.get("data")
        );
        this.id = parse.getAsNumber("id").longValue();
    }

    @Override
    public TypePacket getType() {
        return TypePacket.REQUEST;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    protected void serializeNative(JSONObject jsonObject) {
        super.serializeNative(jsonObject);
        jsonObject.put("id", id);
    }
}
