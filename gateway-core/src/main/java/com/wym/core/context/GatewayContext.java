package com.wym.core.context;

import com.wym.common.config.Rule;
import com.wym.core.request.GatewayRequest;
import com.wym.core.response.GatewayResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
public class GatewayContext extends BaseContext {
    public GatewayRequest request;
    public GatewayResponse response;
    public Rule rule;

    public void setResponse(Object response) {
        this.response = ((GatewayResponse) response);
    }

    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive,
                          GatewayRequest request, Rule rule) {
        super(protocol, nettyCtx, keepAlive);
        this.request = request;
        this.rule = rule;
    }

    @Setter
    @Accessors(chain = true)
    public static class Builder {
        private String protocol;

        private ChannelHandlerContext nettyCtx;

        private GatewayRequest request;

        private Rule rule;

        private boolean keepAlive;

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder nettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Builder request(GatewayRequest request) {
            this.request = request;
            return this;
        }

        public Builder rule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public GatewayContext build() {
            return new GatewayContext(protocol, nettyCtx, keepAlive, request, rule);
        }
    }

    // 获取builder对象
    public static Builder builder() {
        return new Builder();
    }

    // 获取指定的过滤器信息
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    // 获取服务唯一ID
    public String getUniqueId() {
        return request.getUniqueId();
    }

    // 重写父类释放资源, 真正释放
    public boolean releaseRequest() {
        if (requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(request.getFullHttpRequest());
        }
        return true;
    }

    // 获取原始请求对象
    public GatewayRequest getOriginRequest() {
        return request;
    }

    // 获取指定的key的上下文参数，如果没有返回默认值
    public <T> T getRequireAttribute(String key, T defaultValue) {
        return (T) super.attributes.getOrDefault(key, defaultValue);
    }

    // 获取必要的上下文参数
    public <T> T getRequireAttribute(String key) {
        return getRequireAttribute(key, null);
    }
}
