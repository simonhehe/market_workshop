package com.example.mall.repository;

import com.example.mall.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatusAndDeletedAtIsNullOrderBySortAsc(String status);
    List<Product> findByCategoryIdAndStatusAndDeletedAtIsNull(Long categoryId, String status);
    Optional<Product> findByIdAndDeletedAtIsNull(Long id);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'active' AND p.deletedAt IS NULL " +
           "AND (:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'points' THEN p.pointsRequired END ASC, " +
           "CASE WHEN :sortBy = 'sales' THEN p.sales END DESC, " +
           "CASE WHEN :sortBy = 'time' THEN p.createdAt END DESC, " +
           "p.sort ASC")
    List<Product> searchProducts(@Param("keyword") String keyword, 
                                  @Param("categoryId") Long categoryId,
                                  @Param("sortBy") String sortBy);
}
