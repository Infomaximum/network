package com.infomaximum.network.transport.socket;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import com.infomaximum.network.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: Admin
 * Date: 11.09.13
 * Time: 20:33
 * To change this template use File | Settings | File Templates.
 */
public class DiscardServerHandler extends ChannelHandlerAdapter {

    private final static Logger log = LoggerFactory.getLogger(DiscardServerHandler.class);

    private final SocketTransport socketTransport;

    public DiscardServerHandler(SocketTransport socketTransport) {
        this.socketTransport = socketTransport;
    }

//    @Override
//    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//        super.channelRegistered(ctx);
//        socketTransport.fireConnect(ctx, ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress());
//    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        try {
//            socketTransport.fireIncomingPacket(ctx, (Packet)msg);
//        } catch (Exception e) {
//            log.error("error", e);
//        } finally {
//            ReferenceCountUtil.release(msg); // (2)
//        }
//    }

//    @Override
//    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//        super.channelUnregistered(ctx);
//        socketTransport.fireDisconnect(ctx, null, null);
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
