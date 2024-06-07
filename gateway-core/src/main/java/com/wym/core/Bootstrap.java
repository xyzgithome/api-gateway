package com.wym.core;

import com.wym.core.config.Config;
import com.wym.core.config.ConfigLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bootstrap {
    public static void main(String[] args) {
        // 加载网关核心静态配置
        Config config = ConfigLoader.getInstance().load(args);
        log.info("gateway server port: {}", config.getPort());

        // 插件初始化
        // 配置中心管理器初始化，连接配置中心，监听配置的心中、修改、删除
        // 启动容器
        Container container = new Container(config);
        container.start();


        // 连接注册中心，将注册中心实力加载到本地
        // 服务优雅关机
    }

}
