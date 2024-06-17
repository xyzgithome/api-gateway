package com.wym.spring.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class OrderService {
    public String addOrder() {
        return "success";
    }
}
