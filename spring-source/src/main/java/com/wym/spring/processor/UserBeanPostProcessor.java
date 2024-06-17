package com.wym.spring.processor;

import com.wym.spring.service.UserService;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class UserBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanPostProcessor, DestructionAwareBeanPostProcessor {
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if ("userService".equals(beanName)) {
            System.out.println("1. 实例化userService前 postProcessBeforeInstantiation");
        }
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if ("userService".equals(beanName)) {
            System.out.println("3. 实例化userService后 postProcessAfterInstantiation");
            // 需要返回true，不然下面的postProcessorProperties方法不会执行
            return true;
        }
        return false;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        if ("userService".equals(beanName)) {
            System.out.println("4. 实例化userService后 postProcessProperties");
        }
        return null;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if ("userService".equals(beanName)) {
            System.out.println("6. 初始化userService前 postProcessBeforeInitialization");
            // 需要返回bean，不然@PostConstruct注解的方法不会执行
            return bean;
        }
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if ("userService".equals(beanName)) {
            System.out.println("10. 初始化userService后 postProcessAfterInitialization");
        }
        return null;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if ("userService".equals(beanName)) {
            System.out.println("11. 销毁前userService前 postProcessBeforeDestruction");
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        if (bean.getClass().equals(UserService.class)) {
            return true;
        }
        return false;
    }
}
