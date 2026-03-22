package com.example.mall.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "points_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "points", nullable = false)
    private Integer points; // 正数为获得，负数为消费
    
    @Column(name = "type", nullable = false)
    private String type; // 'earn', 'spend', 'adjust', 'refund'
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "related_id")
    private Long relatedId; // 关联的订单ID或其他ID
    
    @Column(name = "related_type")
    private String relatedType; // 'order', 'signin', 'activity', etc.
    
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter; // 操作后的余额
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
