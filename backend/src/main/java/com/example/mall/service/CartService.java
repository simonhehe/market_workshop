package com.example.mall.service;

import com.example.mall.entity.Cart;
import com.example.mall.entity.Product;
import com.example.mall.repository.CartRepository;
import com.example.mall.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    
    public List<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }
    
    @Transactional
    public Cart addToCart(Long userId, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        Optional<Cart> existingCart = cartRepository.findByUserIdAndProductId(userId, productId);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            return cartRepository.save(cart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setIsSelected(1);
            return cartRepository.save(cart);
        }
    }
    
    @Transactional
    public Cart updateQuantity(Long cartId, Integer quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));
        
        if (quantity <= 0) {
            cartRepository.delete(cart);
            return null;
        }
        
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }
    
    @Transactional
    public Cart updateSelection(Long cartId, Long userId, int isSelected) {
        if (isSelected != 0 && isSelected != 1) {
            throw new RuntimeException("isSelected 只能为 0 或 1");
        }
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此购物车项");
        }
        cart.setIsSelected(isSelected);
        return cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
    
    public Map<String, Object> getCartSummary(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        
        int totalCount = cartItems.size();
        int selectedCount = (int) cartItems.stream()
                .filter(item -> item.getIsSelected() == 1)
                .count();
        
        int totalPoints = 0;
        int totalQuantity = 0;
        
        for (Cart item : cartItems) {
            if (item.getIsSelected() == 1) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    totalPoints += product.getPointsRequired() * item.getQuantity();
                    totalQuantity += item.getQuantity();
                }
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", totalCount);
        summary.put("selectedCount", selectedCount);
        summary.put("totalPoints", totalPoints);
        summary.put("totalQuantity", totalQuantity);
        
        return summary;
    }
}
