package com.wym.core.helper;

import com.wym.common.config.*;
import com.wym.common.constants.BasicConst;
import com.wym.common.constants.GatewayConst;
import com.wym.common.constants.GatewayProtocol;
import com.wym.core.context.GatewayContext;
import com.wym.core.request.GatewayRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.RequestBuilder;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RequestHelper {

    public static GatewayContext doContext(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {

        // 构建请求对象GatewayRequest
        GatewayRequest gateWayRequest = doRequest(fullHttpRequest, ctx);

        // 根据请求对象里的uniqueId，获取资源服务信息(也就是服务定义信息)
		ServiceDefinition serviceDefinition = ServiceDefinition.builder()
				.serviceId("demo").enable(true).version("v1").patternPath("**")
				.envType("dev").protocol(GatewayProtocol.HTTP).build();


		// 根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
        ServiceInvoker serviceInvoker = new HttpServiceInvoker();
        serviceInvoker.setInvokerPath(gateWayRequest.getPath());
        serviceInvoker.setTimeout(500);

        // 根据请求对象获取规则
//        Rule rule = getRule(gateWayRequest, serviceDefinition.getServiceId());

        // 构建我们而定GateWayContext对象
        GatewayContext gatewayContext = GatewayContext.builder()
                .protocol(serviceDefinition.getProtocol())
                .nettyCtx(ctx)
                .keepAlive(HttpUtil.isKeepAlive(fullHttpRequest))
                .request(gateWayRequest)
                .rule(new Rule())
                .build();

        // 后续服务发现会整改删除
        gatewayContext.getRequest().setModifyHost("127.0.0.1:8080");

        return gatewayContext;
    }

    /**
     * 构建Request请求对象
     */
    private static GatewayRequest doRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {

        HttpHeaders headers = fullHttpRequest.headers();
        //	从header头获取必须要传入的关键属性 uniqueId
        String uniqueId = headers.get(GatewayConst.UNIQUE_ID);

        String host = headers.get(HttpHeaderNames.HOST);
        HttpMethod method = fullHttpRequest.method();
        String methodName = method.name();
        String uri = fullHttpRequest.uri();
        String clientIp = getClientIp(ctx, fullHttpRequest);
        String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();
        Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri, charset);
        String path = queryStringDecoder.path();

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setHeaders(headers);
        requestBuilder.setMethod(methodName);

//		Map<String,io.netty.handler.codec.http.cookie.Cookie> cookieMap = null;
//		String cookieString = fullHttpRequest.headers().get("Cookie");
//		Set<io.netty.handler.codec.http.cookie.Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
//		if (cookies != null) {
//			cookieMap = cookies.stream().collect(Collectors.toMap(io.netty.handler.codec.http.cookie.Cookie::name,
//					                             Function.identity()));
//		}

        Map<String, List<String>> parameters = null;
        String body = null;
        if (methodName == "GET") {
            parameters = queryStringDecoder.parameters();
            requestBuilder.setQueryParams(parameters);
        } else if (methodName == "POST") {
            body = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
            System.out.println(body);
            requestBuilder.setBody(body);
            requestBuilder.setHeader("Content-Type", "application/json");
        }

        return new GatewayRequest(uniqueId,
                charset,
                clientIp,
                host,
                uri,
                path,
                method,
                parameters,
                body,
                contentType,
                headers,
                fullHttpRequest,
                requestBuilder);
    }

    /**
     * 获取客户端ip
     */
    private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        String xForwardedValue = fullHttpRequest.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);

        String clientIp = null;
        if (StringUtils.isNotEmpty(xForwardedValue)) {
            List<String> values = Arrays.asList(xForwardedValue.split(", "));
            if (values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
                clientIp = values.get(0);
            }
        }
        if (clientIp == null) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = inetSocketAddress.getAddress().getHostAddress();
        }
        return clientIp;
    }
}
