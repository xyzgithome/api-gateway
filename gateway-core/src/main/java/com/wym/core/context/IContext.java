package com.wym.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * 核心上下文接口定义
 */
public interface IContext {
    /**
     * 运行中
     */
    int RUNNING = 0;

    /**
     * 运行过程中发生错误，
     * 对其进行标记，告诉我们请求已经结束，需要返回客户端
     */
    int WRITTEN = 1;

    /**
     * 标记写回成功，防止并发情况下的多次写回
     */
    int COMPLETED = 2;

    /**
     * 标识网关请求结束
     */
    int TERMINATED = -1;

    /**
     * 设置上下文状态为运行中
     */
    void running();

    /**
     * 设置上下文状态为标记写回
     */
    void written();

    /**
     * 设置上下文状态为标记写回成功
     */
    void completed();

    /**
     * 设置上下文状态为请求结束
     */
    void terminated();

    /**
     * 判断网关状态是否运行中
     * @return true false
     */
    boolean isRunning();

    /**
     * 判断网关状态是否标记写回
     * @return true false
     */
    boolean isWritten();

    /**
     * 判断网关状态是否标记写回成功
     * @return true false
     */
    boolean isCompleted();

    /**
     * 判断网关状态是否请求结束
     * @return true false
     */
    boolean isTerminated();

    String getProtocol();
    Object getRequest();
    Object getResponse();
    void setResponse(Object response);
    Throwable getThrowable();
    void setThrowable(Throwable throwable);

    /**
     * 获取netty上线文
     *
     * @return ctx
     */
    ChannelHandlerContext getNettyCtx();

    /**
     * 是否长连接
     *
     * @return true false
     */
    boolean isKeepAlive();

    /**
     * 释放请求资源
     *
     * @return
     */
    boolean releaseRequest();

    /**
     * 设置写回接收回调函数
     *
     * @param consumer
     */
    void setCompleteCallback(Consumer<IContext> consumer);

    /**
     * 执行写回接收回调函数
     *
     * @param consumer
     */
    void invokeCompleteCallback(Consumer<IContext> consumer);

}
