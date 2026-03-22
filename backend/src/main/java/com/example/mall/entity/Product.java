package com.example.mall.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String specifications; // JSON格式
    
    @Column(nullable = false)
    private Double price;
    
    @Column(name = "points_required", nullable = false)
    private Integer pointsRequired;
    
    @Column(name = "stock", columnDefinition = "INTEGER DEFAULT 0")
    private Integer stock = 0;
    
    @Column(name = "warning_stock", columnDefinition = "INTEGER DEFAULT 10")
    private Integer warningStock = 10;
    
    @Column(columnDefinition = "TEXT")
    private String images; // JSON格式存储图片URL数组
    
    @Column(name = "status", columnDefinition = "TEXT DEFAULT 'active'")
    private String status = "active"; // 'active' or 'inactive'
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "sales", columnDefinition = "INTEGER DEFAULT 0")
    private Integer sales = 0;
    
    @Column(name = "views", columnDefinition = "INTEGER DEFAULT 0")
    private Integer views = 0;
    
    @Column(name = "favorites", columnDefinition = "INTEGER DEFAULT 0")
    private Integer favorites = 0;
    
    @Column(name = "exchange_rules", columnDefinition = "TEXT")
    private String exchangeRules;
    
    @Column(name = "sort", columnDefinition = "INTEGER DEFAULT 0")
    private Integer sort = 0;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
