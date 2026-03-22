package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.Category;
import com.example.mall.entity.Order;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    
    // 商品管理
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts(
            @RequestAttribute("role") String role) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        List<Product> products = adminService.getAllProductsForAdmin();
        return ResponseEntity.ok(ApiResponse.success("获取商品列表成功", products));
    }
    
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @RequestAttribute("role") String role,
            @RequestBody Product product) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        try {
            Product created = adminService.createProduct(product);
            return ResponseEntity.ok(ApiResponse.success("商品创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @RequestAttribute("role") String role,
            @RequestBody Map<String, Object> updates) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        try {
            Product updated = adminService.updateProduct(id, updates);
            return ResponseEntity.ok(ApiResponse.success("商品更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @RequestAttribute("role") String role) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        try {
            adminService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("商品删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    // 订单管理
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders(
            @RequestAttribute("role") String role) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        List<Order> orders = adminService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success("获取订单列表成功", orders));
    }
    
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long id,
            @RequestAttribute("role") String role,
            @RequestBody Map<String, String> request) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        try {
            String status = request.get("status");
            Order order = adminService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("订单状态更新成功", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    // 用户管理
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(
            @RequestAttribute("role") String role) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
    }
    
    @PutMapping("/users/{id}/points")
    public ResponseEntity<ApiResponse<User>> updateUserPoints(
            @PathVariable Long id,
            @RequestAttribute("role") String role,
            @RequestBody Map<String, Object> request) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        try {
            Integer points = Integer.valueOf(request.get("points").toString());
            User user = adminService.updateUserPoints(id, points);
            return ResponseEntity.ok(ApiResponse.success("用户积分更新成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Long id,
            @RequestAttribute("role") String role,
            @RequestBody Map<String, String> request) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        try {
            String status = request.get("status");
            User user = adminService.updateUserStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("用户状态更新成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    // 分类管理
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @RequestAttribute("role") String role,
            @RequestBody Category category) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        try {
            Category created = adminService.createCategory(category);
            return ResponseEntity.ok(ApiResponse.success("分类创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long id,
            @RequestAttribute("role") String role,
            @RequestBody Map<String, Object> updates) {
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("AUTH_004", "权限不足"));
        }
        
        try {
            Category updated = adminService.updateCategory(id, updates);
            return ResponseEntity.ok(ApiResponse.success("分类更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
}
