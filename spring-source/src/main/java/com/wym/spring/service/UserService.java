package com.wym.spring.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class UserService implements InitializingBean, DisposableBean,
        BeanNameAware, BeanFactoryAware, EnvironmentAware, ApplicationEventPublisherAware, ApplicationContextAware {
    private OrderService orderService;

    public UserService() {
        System.out.println("2. 调用无参构造实例化userService");
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        System.out.println("5. 属性赋值注入 set");
        this.orderService = orderService;
    }

    @Override
    public void setBeanName(String name) {
        if ("userService".equals(name)) {
            System.out.println("BeanNameAware");
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("BeanFactoryAware");
    }

    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("EnvironmentAware");
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        System.out.println("ApplicationEventPublisherAware");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("ApplicationContextAware");
    }

    @PostConstruct
    private void postConstruct() throws Exception {
        System.out.println("7. 初始化userService @PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("8. 初始化userService InitializingBean.afterPropertiesSet");
    }

    public void customInit() {
        System.out.println("9. 初始化userService custom init");
    }

    @PreDestroy
    public void PreDestroy() {
        System.out.println("12. 容器销毁前 @PreDestroy");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("13. 容器销毁前 DisposableBean.destroy");
    }

    public void customDestroy() {
        System.out.println("14. 容器销毁前 customDestroy");
    }
}
