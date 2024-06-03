package com.wym.core.context;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BaseContext implements IContext {
    protected final String protocol;

    // 状态多线程情况下考虑使用volatile，多线程下可见性和防止指令重排
    protected volatile int status = IContext.RUNNING;

    // Netty上下文
    protected final ChannelHandlerContext nettyCtx;

    // netty上下文参数
    protected final Map<String, Object> attributes = new HashMap<>();

    // 请求过程中发生的异常
    protected Throwable throwable;

    // 是否保持长连接
    protected final boolean keepAlive;

    // 存放回调函数集合
    protected List<Consumer<IContext>> completedCallbackList;

    // 定义是否已经释放资源
    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);

    public BaseContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyCtx = nettyCtx;
        this.keepAlive = keepAlive;
    }

    @Override
    public void running() {
        status = IContext.RUNNING;
    }

    @Override
    public void written() {
        status = IContext.WRITTEN;
    }

    @Override
    public void completed() {
        status = IContext.COMPLETED;
    }

    @Override
    public void terminated() {
        status = IContext.TERMINATED;
    }

    @Override
    public boolean isRunning() {
        return status == IContext.RUNNING;
    }

    @Override
    public boolean isWritten() {
        return status == IContext.WRITTEN;
    }

    @Override
    public boolean isCompleted() {
        return status == IContext.COMPLETED;
    }

    @Override
    public boolean isTerminated() {
        return status == IContext.TERMINATED;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    public void setResponse(Object response) {

    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public ChannelHandlerContext getNettyCtx() {
        return this.nettyCtx;
    }

    @Override
    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    @Override
    public boolean releaseRequest() {
        return false;
    }

    @Override
    public void setCompleteCallback(Consumer<IContext> consumer) {
        if (CollectionUtils.isEmpty(completedCallbackList)) {
            completedCallbackList = new ArrayList<>();
        }
        completedCallbackList.add(consumer);
    }

    @Override
    public void invokeCompleteCallback(Consumer<IContext> consumer) {
        if (CollectionUtils.isEmpty(completedCallbackList)) {
            return;
        }

        completedCallbackList.forEach(completedCallback -> completedCallback.accept(this));
    }
}
