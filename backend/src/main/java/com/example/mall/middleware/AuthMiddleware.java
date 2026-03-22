package com.example.mall.middleware;

import com.example.mall.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthMiddleware extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/",
        "/health",
        "/auth/login",
        "/products",
        "/categories"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // 移除context path前缀（/api）
        String path = requestPath;
        if (path.startsWith("/api")) {
            path = path.substring(4);
            if (path.isEmpty()) {
                path = "/";
            }
        }
        
        final String finalPath = path;
        
        // 检查是否在排除列表中
        boolean shouldExclude = EXCLUDED_PATHS.stream()
                .anyMatch(excludedPath -> finalPath.equals(excludedPath) || finalPath.startsWith(excludedPath + "/"));
        
        if (shouldExclude) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取token
        String token = getTokenFromRequest(request);
        
        if (token == null || !jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"未授权访问\",\"error\":{\"code\":\"AUTH_001\",\"message\":\"请先登录\"}}");
            return;
        }
        
        // 将用户信息存储到request attribute中
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
