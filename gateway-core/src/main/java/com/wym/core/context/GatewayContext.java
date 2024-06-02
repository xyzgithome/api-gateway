package com.wym.core.context;


import com.wym.core.request.IGatewayRequest;
import com.wym.core.response.GatewayResponse;
import io.netty.channel.ChannelHandlerContext;

public class GatewayContext extends BaseContext {
    public IGatewayRequest request;
    public GatewayResponse response;
//    public Rule rule;




    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        super(protocol, nettyCtx, keepAlive);
    }
}
