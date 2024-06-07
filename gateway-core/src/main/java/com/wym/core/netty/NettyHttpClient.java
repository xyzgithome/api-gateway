package com.wym.core.netty;

import com.wym.core.LifeCycle;
import com.wym.core.config.Config;
import com.wym.core.helper.AsyncHttpHelper;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class NettyHttpClient implements LifeCycle {
    private final Config config;

    private final EventLoopGroup eventLoopWorkerGroup;

    private AsyncHttpClient asyncHttpClient;

    public NettyHttpClient(Config config, EventLoopGroup eventLoopWorkerGroup) {
        this.config = config;
        this.eventLoopWorkerGroup = eventLoopWorkerGroup;
        init();
    }

    @Override
    public void init() {
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopWorkerGroup)
                .setConnectTimeout(config.getHttpConnectTimeout())
                .setRequestTimeout(config.getHttpRequestTimeout())
                .setMaxRedirects(config.getHttpMaxRequestRetry())
                .setCompressionEnforced(true)
                .setMaxConnections(config.getHttpMaxConnections())
                .setMaxConnectionsPerHost(config.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(config.getHttpPooledConnectionIdleTimeout())
                .setAllocator(PooledByteBufAllocator.DEFAULT);// 池化的byteBuf分配器

        this.asyncHttpClient = new DefaultAsyncHttpClient(builder.build());
    }

    @Override
    public void start() {
        AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
    }

    @Override
    public void shutdown() {
        if (Objects.isNull(asyncHttpClient)) {
            return;
        }

        try {
            asyncHttpClient.close();
        } catch (IOException e) {
            log.error("NettyHttpClient shutdown error: ", e);
        }
    }
}
