package com.example.mall.service;

import com.example.mall.entity.*;
import com.example.mall.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    
    // 商品管理
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public Product updateProduct(Long id, Map<String, Object> updates) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        if (updates.containsKey("name")) {
            product.setName((String) updates.get("name"));
        }
        if (updates.containsKey("description")) {
            product.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("pointsRequired")) {
            product.setPointsRequired(Integer.valueOf(updates.get("pointsRequired").toString()));
        }
        if (updates.containsKey("stock")) {
            product.setStock(Integer.valueOf(updates.get("stock").toString()));
        }
        if (updates.containsKey("status")) {
            product.setStatus((String) updates.get("status"));
        }
        if (updates.containsKey("categoryId")) {
            product.setCategoryId(Long.valueOf(updates.get("categoryId").toString()));
        }
        
        return productRepository.save(product);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setDeletedAt(java.time.LocalDateTime.now());
        productRepository.save(product);
    }
    
    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }
    
    // 订单管理
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    // 用户管理
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional
    public User updateUserPoints(Long userId, Integer points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        userService.updatePoints(userId, points);
        return userRepository.findById(userId).orElse(user);
    }
    
    @Transactional
    public User updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setStatus(status);
        return userRepository.save(user);
    }
    
    // 分类管理
    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    @Transactional
    public Category updateCategory(Long id, Map<String, Object> updates) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        
        if (updates.containsKey("name")) {
            category.setName((String) updates.get("name"));
        }
        if (updates.containsKey("description")) {
            category.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("sort")) {
            category.setSort(Integer.valueOf(updates.get("sort").toString()));
        }
        if (updates.containsKey("status")) {
            category.setStatus((String) updates.get("status"));
        }
        
        return categoryRepository.save(category);
    }
}
