package com.infomaximum.network.packet;

/**
 * Created by kris on 25.08.16.
 */
public enum TypePacket {

    ASYNC(1),

    REQUEST(2),

    RESPONSE(3);

    private final int id;

    private TypePacket(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TypePacket get(long id) {
        for (TypePacket item: TypePacket.values()) {
            if (item.id==id) return item;
        }
        throw new RuntimeException("Nothing type packet, id: " + id);
    }
}
