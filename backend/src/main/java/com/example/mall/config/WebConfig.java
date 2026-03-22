package com.example.mall.config;

import com.example.mall.middleware.AuthMiddleware;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final AuthMiddleware authMiddleware;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 使用Filter而不是Interceptor，已在AuthMiddleware中实现
    }
}
