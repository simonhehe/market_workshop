package com.example.mall.service;

import com.example.mall.common.BusinessException;
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
                .orElseThrow(() -> new BusinessException("PRODUCT_UNAVAILABLE", "商品不存在或已下架"));
        
        int stock = product.getStock() == null ? 0 : product.getStock();
        int requested = quantity == null ? 0 : quantity;
        
        Optional<Cart> existingCart = cartRepository.findByUserIdAndProductId(userId, productId);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            int newQuantity = cart.getQuantity() + requested;
            if (newQuantity > stock) {
                throw new BusinessException("STOCK_INSUFFICIENT", "库存不足");
            }
            cart.setQuantity(newQuantity);
            return cartRepository.save(cart);
        } else {
            if (requested > stock) {
                throw new BusinessException("STOCK_INSUFFICIENT", "库存不足");
            }
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(requested);
            cart.setIsSelected(1);
            return cartRepository.save(cart);
        }
    }
    
    @Transactional
    public Cart selectItem(Long cartId, Long userId, Integer isSelected) {
        Cart cart = requireOwnedCart(cartId, userId);
        if (isSelected == null || (isSelected != 0 && isSelected != 1)) {
            throw new BusinessException("CART_SELECTION_INVALID", "勾选状态无效");
        }
        cart.setIsSelected(isSelected);
        return cartRepository.save(cart);
    }
    
    @Transactional
    public Cart updateQuantity(Long cartId, Long userId, Integer quantity) {
        Cart cart = requireOwnedCart(cartId, userId);
        
        if (quantity == null || quantity <= 0) {
            cartRepository.delete(cart);
            return null;
        }
        
        Product product = productRepository.findById(cart.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_UNAVAILABLE", "商品不存在或已下架"));
        int stock = product.getStock() == null ? 0 : product.getStock();
        if (quantity > stock) {
            throw new BusinessException("STOCK_INSUFFICIENT", "库存不足");
        }
        
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }
    
    @Transactional
    public void removeFromCart(Long cartId, Long userId) {
        Cart cart = requireOwnedCart(cartId, userId);
        cartRepository.delete(cart);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
    
    public Map<String, Object> getCartSummary(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        
        int totalCount = cartItems.size();
        int selectedCount = (int) cartItems.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsSelected()))
                .count();
        
        int totalPoints = 0;
        int totalQuantity = 0;
        
        for (Cart item : cartItems) {
            if (Integer.valueOf(1).equals(item.getIsSelected())) {
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
    
    private Cart requireOwnedCart(Long cartId, Long userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException("CART_ITEM_FORBIDDEN", "无权操作此购物车商品"));
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException("CART_ITEM_FORBIDDEN", "无权操作此购物车商品");
        }
        return cart;
    }
}
