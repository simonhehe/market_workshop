package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.Product;
import com.example.mall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy) {
        
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
            normalizedKeyword = null;
        }
        
        List<Product> products = productService.searchProducts(normalizedKeyword, categoryId, sortBy);
        
        return ResponseEntity.ok(ApiResponse.success("获取商品列表成功", products));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> {
                    // 增加浏览量
                    product.setViews(product.getViews() + 1);
                    // 这里应该保存，但为了简化，先返回
                    return ResponseEntity.ok(ApiResponse.success("获取商品详情成功", product));
                })
                .orElse(ResponseEntity.badRequest()
                        .body(ApiResponse.error("PROD_001", "商品不存在")));
    }
}
