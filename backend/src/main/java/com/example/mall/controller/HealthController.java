package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "mall-backend");
        data.put("version", "1.0.0");
        data.put("status", "running");
        data.put("timestamp", LocalDateTime.now());
        data.put("endpoints", Map.of(
            "health", "/api/health",
            "auth", "/api/auth/login",
            "products", "/api/products",
            "categories", "/api/categories"
        ));
        
        return ResponseEntity.ok(ApiResponse.success("商城后端API服务", data));
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "healthy");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "mall-backend");
        data.put("version", "1.0.0");
        
        return ResponseEntity.ok(ApiResponse.success("服务运行正常", data));
    }
    
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> test() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "前后端连通测试成功");
        data.put("backend", "Spring Boot 3.2.0");
        data.put("java", "JDK 21");
        data.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(ApiResponse.success("测试接口调用成功", data));
    }
}
