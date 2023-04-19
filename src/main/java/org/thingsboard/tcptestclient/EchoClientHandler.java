package org.thingsboard.tcptestclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
@Slf4j
public class EchoClientHandler extends ChannelInboundHandlerAdapter {

    //private final ByteBuf firstMessage;
    private final int id;

    /**
     * Creates a client-side handler.
     */
    public EchoClientHandler(int id, int size) {
//        firstMessage = Unpooled.buffer(size);
//        for (int i = 0; i < firstMessage.capacity(); i ++) {
//            firstMessage.writeByte((byte) i);
//        }
        this.id = id;
    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) {
//        log.info("channelActive {}", this.id);
//        ctx.writeAndFlush(firstMessage);
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        log.error("exception caught", cause);
        ctx.close();
    }
}