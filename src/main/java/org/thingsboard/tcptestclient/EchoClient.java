package org.thingsboard.tcptestclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
@Component
@Slf4j
public final class EchoClient {

    @Value("${target.host:localhost}")
    String host;

    @Value("${target.port:1883}")
    int port;
    @Value("${target.size:256}")
    int size;
    @Value("${target.clients:1000}")
    int clients;

    List<ChannelFuture> f;
    EventLoopGroup group;

    @PostConstruct
    public void init() throws Exception {
        f = new ArrayList<>(clients);
        // Configure the client.
        group = new NioEventLoopGroup(2);
        log.info("Connecting [{}] TCP clients to the host [{}] port [{}]", clients, host, port);
        for (int i = 0; i < clients; i++) {
            int id = i;
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)

                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            //p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(new EchoClientHandler(id, size));
                        }
                    });

            f.add(b.connect(host, port));
            if (i % 100 == 99) {
                f.get(f.size()-1).sync();
                log.info("Progress: connecting {} from {}", i+1, clients);
            }
        }
        log.info("Syncing [{}] TCP clients to the host [{}] port [{}]", clients, host, port);
        f.get(f.size()-1).sync();
//        f = f.stream().map(channelFuture -> {
//            try {
//                return channelFuture.sync();
//            } catch (InterruptedException e) {
//                log.error("InterruptedException on connect.sync()", e);
//                throw new RuntimeException(e);
//            }
//        }).collect(Collectors.toList());
        log.info("Connected [{}] TCP clients to the host [{}] port [{}]", clients, host, port);
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        log.info("Closing TCP clients [{}]", f.size());
        try {
            f.stream().map(ChannelFuture::channel).map(ChannelOutboundInvoker::close)
                    .forEach(f -> {
                        try {
                            f.sync();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } finally {
            group.shutdownGracefully();
        }
    }
}
