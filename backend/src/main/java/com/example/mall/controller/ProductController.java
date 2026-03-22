package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.Product;
import com.example.mall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        List<Product> products;
        
        if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId);
        } else if (keyword != null || sortBy != null) {
            products = productService.searchProducts(keyword, null, sortBy);
        } else {
            products = productService.getAllProducts();
        }
        
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
