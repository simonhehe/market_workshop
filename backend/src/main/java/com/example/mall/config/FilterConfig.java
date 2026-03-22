package com.example.mall.config;

import com.example.mall.middleware.AuthMiddleware;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {
    
    private final AuthMiddleware authMiddleware;
    
    @Bean
    public FilterRegistrationBean<AuthMiddleware> authFilterRegistration() {
        FilterRegistrationBean<AuthMiddleware> registration = new FilterRegistrationBean<>();
        registration.setFilter(authMiddleware);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("authFilter");
        return registration;
    }
}
