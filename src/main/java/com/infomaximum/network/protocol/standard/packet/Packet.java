package com.infomaximum.network.protocol.standard.packet;

import com.infomaximum.network.packet.IPacket;
import net.minidev.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by kris on 25.08.16.
 */
public abstract class Packet implements IPacket {

    private final JSONObject data;

    protected Packet(JSONObject data){
        this.data=data;
    }

    public abstract TypePacket getType();

    public JSONObject getData() {
        return data;
    }

    @Override
    public String serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", getType().getId());
        serializeNative(jsonObject);
        if (data!=null) jsonObject.put("data", data);
        return jsonObject.toJSONString();
    }

    protected abstract void serializeNative(JSONObject jsonObject);

    public static Packet parse(JSONObject parse) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        TypePacket type = TypePacket.get(parse.getAsNumber("type").intValue());
        if (type == TypePacket.ASYNC) {
            String controller = parse.getAsString("controller");
            String action = parse.getAsString("action");
            JSONObject data = (JSONObject) parse.get("data");
            return new AsyncPacket(controller, action, data);
        } else if (type == TypePacket.REQUEST) {
//            if (extensionRequestPacket == null) {
            return new RequestPacket(parse);
//            } else {
//                return extensionRequestPacket.getConstructor(JSONObject.class).newInstance(parse);
//            }
        } else if (type == TypePacket.RESPONSE) {
            long id = parse.getAsNumber("id").longValue();
            int code = parse.getAsNumber("code").intValue();
            JSONObject data = (JSONObject) parse.get("data");
            return new ResponsePacket(id, code, data);
        } else {
            throw new RuntimeException("Not support type packet: " + type);
        }
    }
}
