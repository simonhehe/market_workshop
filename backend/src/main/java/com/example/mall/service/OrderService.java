package com.example.mall.service;

import com.example.mall.common.BusinessException;
import com.example.mall.entity.*;
import com.example.mall.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private static final int REMARK_MAX_LENGTH = 100;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    
    @Transactional
    public Order createOrder(Long userId, Long addressId, String remark) {
        List<Cart> selectedItems = cartRepository.findByUserId(userId).stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsSelected()))
                .toList();
        
        if (selectedItems.isEmpty()) {
            throw new BusinessException("CART_EMPTY", "请先选择要兑换的商品");
        }
        
        if (addressId == null) {
            throw new BusinessException("ADDR_REQUIRED", "请选择收货地址后再提交订单");
        }
        
        Address address = addressRepository.findById(addressId)
                .filter(item -> item.getDeletedAt() == null && userId.equals(item.getUserId()))
                .orElseThrow(() -> new BusinessException("ADDR_NOT_FOUND", "收货地址无效，请重新选择"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        int totalPoints = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Cart cartItem : selectedItems) {
            int quantity = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
            
            Product product = productRepository.findById(cartItem.getProductId())
                    .filter(item -> item.getDeletedAt() == null)
                    .orElseThrow(() -> new BusinessException("PRODUCT_UNAVAILABLE", "商品已下架，请移除后再提交"));

            if (!"active".equals(product.getStatus())) {
                throw new BusinessException("PRODUCT_UNAVAILABLE", String.format(
                        "商品「%s」已下架，请移除后再提交", product.getName()));
            }
            
            int stock = product.getStock() == null ? 0 : product.getStock();
            if (stock < quantity) {
                throw new BusinessException("STOCK_INSUFFICIENT", String.format(
                        "库存不足：商品「%s」仅剩 %d 件，需要 %d 件", product.getName(), stock, quantity));
            }
            
            int subtotal = product.getPointsRequired() * quantity;
            totalPoints += subtotal;
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImages());
            orderItem.setPointsRequired(product.getPointsRequired());
            orderItem.setQuantity(quantity);
            orderItem.setSubtotalPoints(subtotal);
            
            orderItems.add(orderItem);
        }
        
        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        if (currentPoints < totalPoints) {
            throw new BusinessException("POINTS_INSUFFICIENT", String.format(
                    "积分不足：当前 %d，需支付 %d，还差 %d", currentPoints, totalPoints, totalPoints - currentPoints));
        }
        
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setTotalPoints(totalPoints);
        order.setStatus("pending");
        order.setRemark(normalizeRemark(remark));
        order.setReceiver(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + " " +
                address.getCity() + " " +
                address.getDistrict() + " " +
                address.getDetailAddress());
        
        order = orderRepository.save(order);
        
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemRepository.save(item);
            
            Product product = productRepository.findById(item.getProductId()).orElseThrow();
            product.setStock(product.getStock() - item.getQuantity());
            product.setSales((product.getSales() == null ? 0 : product.getSales()) + item.getQuantity());
            productRepository.save(product);
        }
        
        user.setPoints(currentPoints - totalPoints);
        userRepository.save(user);
        
        PointsHistory pointsHistory = new PointsHistory();
        pointsHistory.setUserId(userId);
        pointsHistory.setPoints(-totalPoints);
        pointsHistory.setType("spend");
        pointsHistory.setDescription("订单消费：" + order.getOrderNo());
        pointsHistory.setRelatedId(order.getId());
        pointsHistory.setRelatedType("order");
        pointsHistory.setBalanceAfter(user.getPoints());
        pointsHistoryRepository.save(pointsHistory);
        
        for (Cart cartItem : selectedItems) {
            cartRepository.delete(cartItem);
        }
        
        return order;
    }
    
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
    }
    
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "订单不存在"));
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("ORDER_FORBIDDEN", "无权操作此订单");
        }
        
        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException("ORDER_STATUS_INVALID", "当前状态不可取消");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        user.setPoints((user.getPoints() == null ? 0 : user.getPoints()) + order.getTotalPoints());
        userRepository.save(user);
        
        PointsHistory pointsHistory = new PointsHistory();
        pointsHistory.setUserId(userId);
        pointsHistory.setPoints(order.getTotalPoints());
        pointsHistory.setType("refund");
        pointsHistory.setDescription("订单取消退款：" + order.getOrderNo());
        pointsHistory.setRelatedId(order.getId());
        pointsHistory.setRelatedType("order");
        pointsHistory.setBalanceAfter(user.getPoints());
        pointsHistoryRepository.save(pointsHistory);
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStock((product.getStock() == null ? 0 : product.getStock()) + item.getQuantity());
                product.setSales(Math.max(0, (product.getSales() == null ? 0 : product.getSales()) - item.getQuantity()));
                productRepository.save(product);
            }
        }
        
        order.setStatus("cancelled");
        return orderRepository.save(order);
    }

    private String normalizeRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String trimmed = remark.trim();
        return trimmed.length() <= REMARK_MAX_LENGTH ? trimmed : trimmed.substring(0, REMARK_MAX_LENGTH);
    }
}
