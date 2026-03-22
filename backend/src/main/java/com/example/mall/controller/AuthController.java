package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("USER_001", "用户名和密码不能为空"));
            }
            
            Map<String, Object> result = authService.login(username, password);
            return ResponseEntity.ok(ApiResponse.success("登录成功", result));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("USER_001", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("SYS_001", "登录失败：" + e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT无状态，客户端删除token即可
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }
}
