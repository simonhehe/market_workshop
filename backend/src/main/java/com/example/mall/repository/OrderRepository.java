package com.example.mall.repository;

import com.example.mall.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
    Optional<Order> findByOrderNoAndDeletedAtIsNull(String orderNo);
    List<Order> findByStatusAndDeletedAtIsNull(String status);
    
    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL " +
           "AND (:userId IS NULL OR o.userId = :userId) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    List<Order> findOrders(@Param("userId") Long userId, @Param("status") String status);
}
