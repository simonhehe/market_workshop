package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.Favorite;
import com.example.mall.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Favorite>>> getUserFavorites(
            @RequestAttribute("userId") Long userId) {
        try {
            List<Favorite> favorites = favoriteService.getUserFavorites(userId);
            return ResponseEntity.ok(ApiResponse.success("获取收藏列表成功", favorites));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Favorite>> addFavorite(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            String tag = request.containsKey("tag") ? request.get("tag").toString() : null;
            
            Favorite favorite = favoriteService.addFavorite(userId, productId, tag);
            return ResponseEntity.ok(ApiResponse.success("商品已添加到收藏夹", favorite));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            favoriteService.removeFavorite(id, userId);
            return ResponseEntity.ok(ApiResponse.success("商品已从收藏夹移除", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavorite(
            @PathVariable Long productId,
            @RequestAttribute("userId") Long userId) {
        try {
            boolean isFavorite = favoriteService.isFavorite(userId, productId);
            Map<String, Boolean> result = new HashMap<>();
            result.put("isFavorite", isFavorite);
            return ResponseEntity.ok(ApiResponse.success("检查收藏状态成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
}
