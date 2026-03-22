package com.example.mall.service;

import com.example.mall.entity.PointsHistory;
import com.example.mall.repository.PointsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsHistoryService {
    
    private final PointsHistoryRepository pointsHistoryRepository;
    
    public List<PointsHistory> getUserPointsHistory(Long userId) {
        return pointsHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<PointsHistory> getUserPointsHistoryByType(Long userId, String type) {
        return pointsHistoryRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
    }
}
