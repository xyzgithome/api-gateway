package com.wym.core.netty;

import com.wym.common.utils.RemotingUtil;
import com.wym.core.LifeCycle;
import com.wym.core.config.Config;
import com.wym.core.netty.processor.NettyProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;

@Slf4j
public class NettyHttpServer implements LifeCycle {
    private final Config config;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup eventLoopBossGroup;

    @Getter
    private EventLoopGroup eventLoopWorkerGroup;

    private final NettyProcessor nettyProcessor;

    public NettyHttpServer(Config config, NettyProcessor nettyProcessor) {
        this.config = config;
        this.nettyProcessor = nettyProcessor;
        init();
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (useEpoll()) {
            this.eventLoopBossGroup = new EpollEventLoopGroup(
                    config.getEventLoopGroupBossNum(), new DefaultThreadFactory("netty-boss-epoll"));
            this.eventLoopWorkerGroup = new EpollEventLoopGroup(
                    config.getEventLoopGroupWorkerNum(), new DefaultThreadFactory("netty-worker-epoll"));
        } else {
            this.eventLoopBossGroup = new NioEventLoopGroup(
                    config.getEventLoopGroupBossNum(), new DefaultThreadFactory("netty-boss-nio"));
            this.eventLoopWorkerGroup = new NioEventLoopGroup(
                    config.getEventLoopGroupWorkerNum(), new DefaultThreadFactory("netty-worker-nio"));
        }
    }

    private boolean useEpoll(){
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        this.serverBootstrap
                .group(eventLoopBossGroup, eventLoopWorkerGroup)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(config.getPort()))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(config.getMaxContentLength()))
                                .addLast(new NettyServerConnectManagerHandler())
                                .addLast(new NettyHttpServerHandler(nettyProcessor));
                    }
                });
        try {
            this.serverBootstrap.bind().sync();
            log.info("netty startup on port {}", config.getPort());
        } catch (InterruptedException e) {
            log.error("netty startup fail, error: ", e);
        }
    }

    @Override
    public void shutdown() {
        if (Objects.nonNull(eventLoopBossGroup)) {
            eventLoopWorkerGroup.shutdownGracefully();
        }
        if (Objects.nonNull(eventLoopWorkerGroup)){
            eventLoopWorkerGroup.shutdownGracefully();
        }
    }
}
