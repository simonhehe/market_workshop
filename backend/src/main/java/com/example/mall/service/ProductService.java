package com.example.mall.service;

import com.example.mall.entity.Product;
import com.example.mall.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findByStatusAndDeletedAtIsNullOrderBySortAsc("active");
    }
    
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndStatusAndDeletedAtIsNull(categoryId, "active");
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findByIdAndDeletedAtIsNull(id);
    }
    
    public List<Product> searchProducts(String keyword, Long categoryId, String sortBy) {
        return productRepository.searchProducts(keyword, categoryId, sortBy);
    }
    
    @Service
    public static class ProductDetail {
        private Product product;
        private String categoryName;
        
        // Getters and setters
        public Product getProduct() { return product; }
        public void setProduct(Product product) { this.product = product; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    }
}
