package com.infomaximum.network.transport.socket.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import com.infomaximum.network.packet.Packet;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * Created by user on 19.03.2017.
 */
public class PacketEncoder extends MessageToMessageEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) throws Exception {
        ByteBuf byteBuf = Unpooled.copiedBuffer(packet.serialize().getBytes());
        out.add(byteBuf);
    }
}
