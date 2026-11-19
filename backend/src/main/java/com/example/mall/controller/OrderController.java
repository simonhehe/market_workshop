package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.common.BusinessException;
import com.example.mall.entity.Order;
import com.example.mall.entity.OrderItem;
import com.example.mall.repository.OrderItemRepository;
import com.example.mall.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> request) {
        try {
            Long addressId = request.containsKey("addressId") 
                    ? Long.valueOf(request.get("addressId").toString()) 
                    : null;
            String remark = request.containsKey("remark") 
                    ? request.get("remark").toString() 
                    : null;
            
            Order order = orderService.createOrder(userId, addressId, remark);
            return ResponseEntity.ok(ApiResponse.success("订单创建成功", order));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ORDER_001", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(
            @RequestAttribute("userId") Long userId) {
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(ApiResponse.success("获取订单列表成功", orders));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ORDER_002", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderDetail(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(id);
            
            if (orderOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("ORDER_NOT_FOUND", "订单不存在"));
            }
            
            Order order = orderOpt.get();
            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("ORDER_FORBIDDEN", "无权查看此订单"));
            }
            
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("order", order);
            result.put("orderItems", orderItems);
            
            return ResponseEntity.ok(ApiResponse.success("获取订单详情成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ORDER_002", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            Order order = orderService.cancelOrder(id, userId);
            return ResponseEntity.ok(ApiResponse.success("订单取消成功", order));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ORDER_002", e.getMessage()));
        }
    }
}
