package com.wym.core.netty.processor;

import com.wym.common.enums.ResponseCode;
import com.wym.common.exception.BaseException;
import com.wym.common.exception.ConnectException;
import com.wym.common.exception.ResponseException;
import com.wym.core.config.ConfigLoader;
import com.wym.core.context.GatewayContext;
import com.wym.core.context.HttpRequestWrapper;
import com.wym.core.helper.AsyncHttpHelper;
import com.wym.core.helper.RequestHelper;
import com.wym.core.helper.ResponseHelper;
import com.wym.core.response.GatewayResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Slf4j
public class NettyCoreProcessor implements NettyProcessor {
    @Override
    public void process(HttpRequestWrapper wrapper) {
        FullHttpRequest request = wrapper.getRequest();
        ChannelHandlerContext ctx = wrapper.getCtx();

        try {
            GatewayContext gatewayContext = RequestHelper.doContext(request, ctx);

            route(gatewayContext);
        } catch (BaseException e) {
            log.error("processor error {} {}", e.getCode().getCode(), e.getCode().getMessage());
            FullHttpResponse response = ResponseHelper.getProcessFailHttpResponse(e.getCode());
            doWriteAndRelease(ctx, request, response);
        } catch (Throwable t) {
            log.error("processor unknow error, ", t);
            FullHttpResponse response = ResponseHelper.getProcessFailHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(ctx, request, response);
        }
    }

    // 回写数据以及释放资源
    private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        // 释放资源后关闭channel
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

        // 释放request里面的一些bytebuf
        ReferenceCountUtil.release(request);
    }

    private void route(GatewayContext gatewayContext) {
        Request request = gatewayContext.getRequest().build();

        CompletableFuture<Response> completableFuture = AsyncHttpHelper.getInstance().executeRequest(request);

        // 是否是单异步
        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();

        if (whenComplete) {
            completableFuture.whenComplete((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            });
            return;
        }

        // 双异步
        completableFuture.whenCompleteAsync((response, throwable) -> {
            complete(request, response, throwable, gatewayContext);
        });
    }

    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext) {
        // 释放资源
        gatewayContext.releaseRequest();

        try {
            if (Objects.isNull(throwable)) {
                gatewayContext.setResponse(GatewayResponse.buildGatewayResponse(response));
                return;
            }

            String url = request.getUrl();

            if (throwable instanceof TimeoutException) {
                log.error("complete timeout {}", url);
                gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                return;
            }

            gatewayContext.setThrowable(new ConnectException(
                    throwable, gatewayContext.getUniqueId(), url, ResponseCode.HTTP_RESPONSE_ERROR));
        } catch (Throwable t) {
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("complete error, ", t);
        } finally {
            gatewayContext.written();
            ResponseHelper.writeResponse(gatewayContext);
        }
    }
}
