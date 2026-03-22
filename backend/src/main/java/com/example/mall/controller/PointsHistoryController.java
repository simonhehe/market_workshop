package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.PointsHistory;
import com.example.mall.service.PointsHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/points/history")
@RequiredArgsConstructor
public class PointsHistoryController {
    
    private final PointsHistoryService pointsHistoryService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<PointsHistory>>> getPointsHistory(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String type) {
        try {
            List<PointsHistory> history;
            if (type != null) {
                history = pointsHistoryService.getUserPointsHistoryByType(userId, type);
            } else {
                history = pointsHistoryService.getUserPointsHistory(userId);
            }
            return ResponseEntity.ok(ApiResponse.success("获取积分历史成功", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
}
