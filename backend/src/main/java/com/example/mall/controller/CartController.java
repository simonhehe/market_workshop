package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.Cart;
import com.example.mall.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCart(
            @RequestAttribute("userId") Long userId) {
        try {
            List<Cart> cartItems = cartService.getCartByUserId(userId);
            Map<String, Object> summary = cartService.getCartSummary(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("cartItems", cartItems);
            result.put("summary", summary);
            
            return ResponseEntity.ok(ApiResponse.success("获取购物车成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CART_001", e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = request.containsKey("quantity") 
                    ? Integer.valueOf(request.get("quantity").toString()) 
                    : 1;
            
            Cart cart = cartService.addToCart(userId, productId, quantity);
            return ResponseEntity.ok(ApiResponse.success("商品已添加到购物车", cart));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CART_001", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Cart>> updateQuantity(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            Cart cart = cartService.updateQuantity(id, quantity);
            
            if (cart == null) {
                return ResponseEntity.ok(ApiResponse.success("商品已从购物车移除", null));
            }
            
            return ResponseEntity.ok(ApiResponse.success("购物车商品数量更新成功", cart));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CART_001", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/select")
    public ResponseEntity<ApiResponse<Cart>> selectCartItem(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer isSelected = Integer.valueOf(request.get("isSelected").toString());
            Cart cart = cartService.selectCartItem(id, userId, isSelected);
            return ResponseEntity.ok(ApiResponse.success("购物车勾选状态更新成功", cart));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CART_001", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable Long id) {
        try {
            cartService.removeFromCart(id);
            return ResponseEntity.ok(ApiResponse.success("购物车商品删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CART_001", e.getMessage()));
        }
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestAttribute("userId") Long userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok(ApiResponse.success("购物车已清空", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CART_001", e.getMessage()));
        }
    }
}
