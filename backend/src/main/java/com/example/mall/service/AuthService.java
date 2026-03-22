package com.example.mall.service;

import com.example.mall.entity.User;
import com.example.mall.repository.UserRepository;
import com.example.mall.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public Map<String, Object> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsernameAndDeletedAtIsNull(username);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        User user = userOpt.get();
        
        // 验证密码（对于测试账号，使用明文比较；生产环境应使用加密）
        boolean passwordValid = false;
        if (user.getPassword().startsWith("$2")) {
            // BCrypt加密的密码
            passwordValid = passwordEncoder.matches(password, user.getPassword());
        } else {
            // 明文密码（测试账号）
            passwordValid = user.getPassword().equals(password);
        }
        
        if (!passwordValid) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (!"active".equals(user.getStatus())) {
            throw new RuntimeException("账户已被禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userType", user.getRole());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("expiresIn", 86400); // 24小时
        
        return result;
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}
