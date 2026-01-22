package com.tencent.cloudrun.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", LocalDateTime.now());
        health.put("framework", "Spring Boot");
        health.put("version", getClass().getPackage().getImplementationVersion());
        health.put("java_version", System.getProperty("java.version"));
        
        return health;
    }

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "欢迎使用 Spring Boot CloudBase 应用！");
        response.put("status", "running");
        
        return response;
    }
}
