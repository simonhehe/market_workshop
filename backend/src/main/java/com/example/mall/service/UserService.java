package com.example.mall.service;

import com.example.mall.entity.User;
import com.example.mall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    public Map<String, Object> getUserInfo(Long userId) {
        User user = getUserById(userId);
        
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("phone", user.getPhone());
        info.put("points", user.getPoints());
        info.put("role", user.getRole());
        info.put("department", user.getDepartment());
        info.put("position", user.getPosition());
        info.put("avatar", user.getAvatar());
        info.put("createdAt", user.getCreatedAt());
        
        return info;
    }
    
    @Transactional
    public User updateUserInfo(Long userId, Map<String, Object> updates) {
        User user = getUserById(userId);
        
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("department")) {
            user.setDepartment((String) updates.get("department"));
        }
        if (updates.containsKey("position")) {
            user.setPosition((String) updates.get("position"));
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void updatePoints(Long userId, Integer points) {
        User user = getUserById(userId);
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
    }
}
