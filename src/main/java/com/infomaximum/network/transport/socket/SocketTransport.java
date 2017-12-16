package com.infomaximum.network.transport.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import com.infomaximum.network.packet.Packet;
import com.infomaximum.network.transport.Transport;
import com.infomaximum.network.transport.TypeTransport;
import com.infomaximum.network.transport.socket.builder.SocketBuilderTransport;
import com.infomaximum.network.transport.socket.handler.PacketDecoder;
import com.infomaximum.network.transport.socket.handler.PacketEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: Admin
 * Date: 11.09.13
 * Time: 20:14
 * To change this template use File | Settings | File Templates.
 */
public class SocketTransport extends Transport<ChannelHandlerContext> {

    private final static Logger log = LoggerFactory.getLogger(SocketTransport.class);

    private final SocketBuilderTransport socketBuilderTransport;
    private ServerBootstrap serverBootstrap;

    public SocketTransport(final SocketBuilderTransport socketBuilderTransport) throws InterruptedException {
        this.socketBuilderTransport=socketBuilderTransport;
        run();
    }

    private void run() throws InterruptedException {
        final SocketTransport socketTransport = this;
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
//        try {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline channelPipeline = ch.pipeline();
                            channelPipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.nulDelimiter()));//Разделитель фреймов
//                            channelPipeline.addLast(new LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength));//Разделитель фреймов

                            channelPipeline.addLast(new StringDecoder());
                            channelPipeline.addLast(new PacketDecoder());

                            channelPipeline.addLast(new StringEncoder());
                            channelPipeline.addLast(new PacketEncoder());

                            channelPipeline.addLast(new DiscardServerHandler(socketTransport));


//                            ChannelPipeline channelPipeline = ch.pipeline();
//                            channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
//                            channelPipeline.addLast(new PacketDecoder());
////                            channelPipeline.addLast(new ProtobufDecoder(ProtoBuf.Packet.getDefaultInstance()));
//                            channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
//                            channelPipeline.addLast(new PacketEncoder());
////                            channelPipeline.addLast(new ProtobufEncoder());
//                            channelPipeline.addLast(new DiscardServerHandler(socketTransport));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .localAddress(socketBuilderTransport.getPort());

        serverBootstrap.bind().sync();
    }

    @Override
    public TypeTransport getType() {
        return TypeTransport.SOCKET;
    }

    @Override
    public Future<Void> send(ChannelHandlerContext channelHandlerContext, Packet packet) throws IOException {
        channelHandlerContext.writeAndFlush(packet);

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        completableFuture.complete(null);
        return completableFuture;
    }

    @Override
    public void close(ChannelHandlerContext channelHandlerContext) throws IOException {
        channelHandlerContext.close();
    }

    @Override
    public void destroy() {
        serverBootstrap.group().shutdownGracefully();
        serverBootstrap.childGroup().shutdownGracefully();
    }
}
