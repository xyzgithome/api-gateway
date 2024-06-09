package com.wym.backend.http.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PingController {
    @GetMapping("/http-server/ping")
    public String ping() {
        return "pong";
    }
}
