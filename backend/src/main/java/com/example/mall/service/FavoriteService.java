package com.example.mall.service;

import com.example.mall.entity.Favorite;
import com.example.mall.entity.Product;
import com.example.mall.repository.FavoriteRepository;
import com.example.mall.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    
    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Transactional
    public Favorite addFavorite(Long userId, Long productId, String tag) {
        // 检查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查是否已收藏
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            throw new RuntimeException("商品已在收藏夹中");
        }
        
        // 增加商品收藏数
        product.setFavorites(product.getFavorites() + 1);
        productRepository.save(product);
        
        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setTag(tag);
        
        return favoriteRepository.save(favorite);
    }
    
    @Transactional
    public void removeFavorite(Long favoriteId, Long userId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("收藏记录不存在"));
        
        if (!favorite.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此收藏");
        }
        
        // 减少商品收藏数
        Product product = productRepository.findById(favorite.getProductId()).orElse(null);
        if (product != null) {
            product.setFavorites(Math.max(0, product.getFavorites() - 1));
            productRepository.save(product);
        }
        
        favoriteRepository.delete(favorite);
    }
    
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}
