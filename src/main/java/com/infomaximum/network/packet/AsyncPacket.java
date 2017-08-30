package com.infomaximum.network.packet;

import net.minidev.json.JSONObject;

/**
 * Created by kris on 26.08.16.
 */
public class AsyncPacket extends TargetPacket {

    public AsyncPacket(String controller, String action, JSONObject data) {
        super(controller, action, data);
    }

    @Override
    public TypePacket getType() {
        return TypePacket.ASYNC;
    }

}
