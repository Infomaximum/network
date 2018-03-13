package com.infomaximum.network.transport.socket.handler;

import com.infomaximum.network.Network;
import com.infomaximum.network.NetworkImpl;
import com.infomaximum.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

import java.util.List;

/**
 * Created by user on 18.03.2017.
 */
public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] array;
        if (msg.hasArray()) {
            array = msg.array();
        } else {
            throw new RuntimeException("Not support");
        }

        String str = new String(array);
        str = str.substring(str.indexOf('{'));//TODO хак от protobuff

        JSONObject incoming = (JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(str);
        Packet packet = NetworkImpl.instance.parsePacket(incoming);
        out.add(packet);
    }
}
