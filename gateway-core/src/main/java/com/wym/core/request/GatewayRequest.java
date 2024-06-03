package com.wym.core.request;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.wym.common.constants.BasicConst;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.*;

public class GatewayRequest implements IGatewayRequest {
    // 服务唯一id
    @Getter
    private final String uniqueId;

    // 进入网关的开始时间
    private final long beginTime;

    // 进入网关的结束时间
    private final long endTime;

    // 字符集
    private final Charset charset;

    // 客户端ip
    private final String clientIp;

    // 服务端主机
    private final String host;

    // 服务端请求路径 /xxx/xx/x
    private final String path;

    // 统一资源标识符   /xxx/xx/x?attr1=1&attr2=2
    private final String uri;

    // 请求方式 post/get/put/delete
    private final HttpMethod method;

    // 请求格式 json/xml...
    private final String contentType;

    // 请求头
    private final HttpHeaders headers;

    // 参数解析器
    private final QueryStringDecoder queryStringDecoder;

    // 校验请求是否是一个完整的http请求
    @Getter
    private final FullHttpRequest fullHttpRequest;

    // 请求体
    private String requestBody;

    // cookie
    private Map<String, Cookie> cookieMap;

    // post请求参数
    private Map<String, List<String>> postParameters;

    // 可修改的scheme 默认是http协议
    private String modifyScheme;

    // 可修改的主机名
    private String modifyHost;

    // 可修改的请求路径
    private String modifyPath;

    // 构建下游请求时的http构建器
    private final RequestBuilder requestBuilder;


    public GatewayRequest(String uniqueId, long endTime, Charset charset, String clientIp,
                          String host, String uri, HttpMethod method, String contentType,
                          HttpHeaders headers, QueryStringDecoder queryStringDecoder,
                          FullHttpRequest fullHttpRequest) {
        // 不变属性赋值
        this.uniqueId = uniqueId;
        this.beginTime = System.currentTimeMillis();
        this.endTime = endTime;
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.path = queryStringDecoder.path();
        this.uri = uri;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.queryStringDecoder = new QueryStringDecoder(uri, charset);
        this.fullHttpRequest = fullHttpRequest;
        this.requestBuilder = new RequestBuilder()
                .setMethod(this.method.name())
                .setHeaders(this.headers)
                .setQueryParams(queryStringDecoder.parameters());
        ByteBuf content = fullHttpRequest.content();
        if (Objects.nonNull(content)) {
            this.requestBuilder.setBody(content.nioBuffer());
        }

        // 可变的属性赋值
//        this.requestBody = requestBody;
//        this.cookieMap = cookieMap;
//        this.postParameters = postParameters;
        this.modifyScheme = BasicConst.HTTP_PREFIX_SEPARATOR;
        this.modifyHost = host;
        this.modifyPath = path;

    }

    // 获取并设置 请求体 requestBody
    public String getRequestBody() {
        if (StringUtils.isBlank(requestBody)) {
            requestBody = fullHttpRequest.content().toString(charset);
        }

        return requestBody;
    }

    // 设置cookieMap, 并获取cookie
    public Cookie getCookie(String name) {
        if (CollectionUtils.isEmpty(cookieMap)) {
            cookieMap = new HashMap<>();
            String cookieStr = headers.get(HttpHeaderNames.COOKIE);
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);

            for (Cookie cookie : cookies) {
                cookieMap.put(name, cookie);
            }
        }

        return cookieMap.get(name);
    }

    // 获取指定名称的参数值
    public List<String> getQueryParametersMultiple(String name) {
        return queryStringDecoder.parameters().get(name);
    }

    // 获取post请求指定名称的参数值
    public List<String> getPostParametersMultiple(String name) {
        if (!isFormPost() && !isJsonPost()) {
            return Collections.emptyList();
        }

        String requestBody = getRequestBody();

        // 请求体是表单格式
        if (isFormPost()) {
            if (CollectionUtils.isEmpty(postParameters)) {
                // hasPath true : /xxx/xx/x?attr1=1&attr2=2
                // hasPath false : attr1=1&attr2=2
                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(requestBody, false);
                postParameters = queryStringDecoder.parameters();
            }

            return postParameters.get(name);
        }

        // 请求体类型是json
        return Lists.newArrayList(JsonPath.read(requestBody, name).toString());
    }

    public boolean isFormPost() {
        return HttpMethod.POST.equals(method) &&
                (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                        contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    public boolean isJsonPost() {
        return HttpMethod.POST.equals(method) && contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }

    @Override
    public void setModifyHost(String host) {
        this.modifyHost = host;
    }

    @Override
    public String getModifyHost() {
        return this.modifyHost;
    }

    @Override
    public void setModifyPath(String path) {
        this.modifyPath = path;
    }

    @Override
    public String getModifyPath() {
        return this.modifyPath;
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        requestBuilder.setHeader(name, value);
    }

    @Override
    public void addQueryParam(String name, String value) {
        requestBuilder.addQueryParam(name, value);
    }

    @Override
    public void addFormParam(String name, String value) {
        if (!isFormPost()) {
            return;
        }

        requestBuilder.addFormParam(name, value);
    }

    @Override
    public void addOrReplaceCookie(org.asynchttpclient.cookie.Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    @Override
    public void setRequestTimeout(int requestTimeout) {
        requestBuilder.setRequestTimeout(requestTimeout);
    }

    @Override
    public String getFinalURL() {
        return this.modifyScheme.concat(this.modifyHost).concat(this.modifyPath);
    }

    @Override
    public Request build() {
        return requestBuilder.setUrl(getFinalURL()).build();
    }
}
