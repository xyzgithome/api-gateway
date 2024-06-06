package com.wym.core.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wym.common.enums.ResponseCode;
import com.wym.common.utils.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Getter;
import lombok.Setter;
import org.asynchttpclient.Response;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Setter
@Getter
public class GatewayResponse {
    // 响应头
    private HttpHeaders responseHeaders = new DefaultHttpHeaders();

    // 额外响应头
    private HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

    // 响应内容
    private String content;

    // 返回响应状态码
    private HttpResponseStatus httpResponseStatus;

    // 异步响应对象
    private Response futureResponse;

    public GatewayResponse() {}

    // 设置相应头
    public void putHeader(CharSequence key, CharSequence val){
        responseHeaders.add(key, val);
    }

    // 构建异步的响应对象
    public static GatewayResponse buildGatewayResponse(Response futureResponse){
        GatewayResponse response = new GatewayResponse();
        response.setFutureResponse(futureResponse);
        response.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return response;
    }

    // 构建失败JSON类型的响应信息
    public static GatewayResponse buildGatewayResponse(ResponseCode code, Object...args){
        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus(code.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON.concat(";charset=utf-8"));

        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, code.getStatus().code());
        objectNode.put(JSONUtil.CODE, code.getCode());
        objectNode.put(JSONUtil.MESSAGE, code.getMessage());
        response.setContent(JSONUtil.toJSONString(objectNode));

        return response;
    }

    // 构建成功响应对象
    public static GatewayResponse buildGatewayResponse(Object data){
        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON.concat(";charset=utf-8"));

        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA, data);
        response.setContent(JSONUtil.toJSONString(objectNode));

        return response;
    }
}
