package com.wym.spring.config;

import com.wym.spring.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.wym.spring")
public class MainConfig {
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public UserService userService() {
        return new UserService();
    }
}
