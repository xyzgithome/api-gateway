package com.wym.core.request;

import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.Request;

public interface IGatewayRequest {

    // 修改目标服务主机
    void setModifyHost(String host);

    // 获取目标服务主机
    String getModifyHost();

    // 设置目标服务路径
    void setModifyPath(String path);

    // 获取目标服务路径
    String getModifyPath();

    // 添加请求头信息
    void addHeader(CharSequence name, String value);

    // 设置请求头信息
    void setHeader(CharSequence name, String value);

    // 添加请求参数 get
    void addQueryParam(String name, String value);

    // 添加请求参数 post
    void addFormParam(String name, String value);

    // 添加或者替换cookie
    void addOrReplaceCookie(org.asynchttpclient.cookie.Cookie cookie);

    // 设置超时时间
    void setRequestTimeout(int requestTimeout);

    // 获取最终请求路径，包含请求参数
    // http://localhost:8080/api/admin?name=Tom
    String getFinalURL();

    // 构建请求对象
    Request build();
}
