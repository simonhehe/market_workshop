package com.example.mall.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", unique = true, nullable = false)
    private String orderNo;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "address_id")
    private Long addressId;
    
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;
    
    @Column(name = "status", columnDefinition = "TEXT DEFAULT 'pending'")
    private String status = "pending"; // 'pending', 'processing', 'shipped', 'completed', 'cancelled'
    
    @Column(name = "receiver")
    private String receiver;
    
    @Column(name = "receiver_phone")
    private String receiverPhone;
    
    @Column(name = "receiver_address", columnDefinition = "TEXT")
    private String receiverAddress;
    
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
    
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
        if (orderNo == null) {
            orderNo = "ORD" + System.currentTimeMillis();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
