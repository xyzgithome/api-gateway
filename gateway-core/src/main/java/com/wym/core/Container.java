package com.wym.core;

import com.wym.core.config.Config;
import com.wym.core.netty.NettyHttpClient;
import com.wym.core.netty.NettyHttpServer;
import com.wym.core.netty.processor.NettyCoreProcessor;
import com.wym.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 网关核心容器
 * 由于是组件，所以需要实现生命周期接口
 */
@Slf4j
public class Container implements LifeCycle {
    private final Config config;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        this.nettyProcessor = new NettyCoreProcessor();
        this.nettyHttpServer = new NettyHttpServer(config, nettyProcessor);
        this.nettyHttpClient = new NettyHttpClient(config, nettyHttpServer.getEventLoopWorkerGroup());
    }

    @Override
    public void start() {
        nettyHttpServer.start();
        nettyHttpClient.start();
        log.info("api gateway started!");
    }

    @Override
    public void shutdown() {
        nettyHttpServer.shutdown();
        nettyHttpClient.shutdown();
        log.info("api gateway shutdown!");
    }
}
