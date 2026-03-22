package com.example.mall.repository;

import com.example.mall.entity.PointsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsHistoryRepository extends JpaRepository<PointsHistory, Long> {
    List<PointsHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PointsHistory> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
}
