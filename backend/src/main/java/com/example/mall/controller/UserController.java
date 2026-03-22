package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.User;
import com.example.mall.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserInfo(
            @RequestAttribute("userId") Long userId) {
        try {
            Map<String, Object> userInfo = userService.getUserInfo(userId);
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("USER_002", e.getMessage()));
        }
    }
    
    @PutMapping("/info")
    public ResponseEntity<ApiResponse<User>> updateUserInfo(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> updates) {
        try {
            User user = userService.updateUserInfo(userId, updates);
            return ResponseEntity.ok(ApiResponse.success("更新用户信息成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("USER_002", e.getMessage()));
        }
    }
}
