package com.wym.core.netty;

import com.wym.core.context.HttpRequestWrapper;
import com.wym.core.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.concurrent.EventExecutorGroup;

public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter {
    private final NettyProcessor nettyProcessor;

    public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
        this.nettyProcessor = nettyProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;

        HttpRequestWrapper wrapper = new HttpRequestWrapper();
        wrapper.setRequest(request);
        wrapper.setCtx(ctx);

        nettyProcessor.process(wrapper);
    }
}
