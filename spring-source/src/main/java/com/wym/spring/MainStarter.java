package com.wym.spring;


import com.wym.spring.config.MainConfig;
import com.wym.spring.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainStarter {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MainConfig.class);

        // 从DefaultListableBeanFactory中获取bean
        UserService userService = context.getBean(UserService.class);
//        UserService userService1 = context.getBean(UserService.class);

//        System.out.println(userService);
//        System.out.println(userService1);

        context.close();



    }

}
