package com.example.mall.service;

import com.example.mall.entity.*;
import com.example.mall.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    /** 用户侧列表筛选允许的状态（与实体注释一致；管理员仍可写入其它字符串）。 */
    private static final Set<String> ALLOWED_LIST_STATUS_FILTER = Set.of(
            "pending", "processing", "shipped", "completed", "cancelled");
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PointsHistoryRepository pointsHistoryRepository;
    private final UserService userService;
    
    @Transactional
    public Order createOrder(Long userId, Long addressId, String remark) {
        // 获取购物车中选中的商品
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        List<Cart> selectedItems = cartItems.stream()
                .filter(item -> item.getIsSelected() == 1)
                .toList();
        
        if (selectedItems.isEmpty()) {
            throw new RuntimeException("购物车中没有选中的商品");
        }
        
        // 计算总积分
        int totalPoints = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Cart cartItem : selectedItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("商品 " + product.getName() + " 库存不足");
            }
            
            int subtotal = product.getPointsRequired() * cartItem.getQuantity();
            totalPoints += subtotal;
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(null); // 稍后设置
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImages());
            orderItem.setPointsRequired(product.getPointsRequired());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotalPoints(subtotal);
            
            orderItems.add(orderItem);
        }
        
        // 检查用户积分
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (user.getPoints() < totalPoints) {
            throw new RuntimeException("积分不足，当前积分：" + user.getPoints() + "，需要积分：" + totalPoints);
        }
        
        // 获取收货地址
        Address address = null;
        if (addressId != null) {
            address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("收货地址不存在"));
        }
        
        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setTotalPoints(totalPoints);
        order.setStatus("pending");
        order.setRemark(remark);
        
        if (address != null) {
            order.setReceiver(address.getReceiver());
            order.setReceiverPhone(address.getPhone());
            order.setReceiverAddress(address.getProvince() + " " + 
                    address.getCity() + " " + 
                    address.getDistrict() + " " + 
                    address.getDetailAddress());
        }
        
        order = orderRepository.save(order);
        
        // 创建订单项
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemRepository.save(item);
            
            // 减少库存
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() - item.getQuantity());
                product.setSales(product.getSales() + item.getQuantity());
                productRepository.save(product);
            }
        }
        
        // 扣除积分
        userService.updatePoints(userId, -totalPoints);
        
        // 记录积分历史
        PointsHistory pointsHistory = new PointsHistory();
        pointsHistory.setUserId(userId);
        pointsHistory.setPoints(-totalPoints);
        pointsHistory.setType("spend");
        pointsHistory.setDescription("订单消费：" + order.getOrderNo());
        pointsHistory.setRelatedId(order.getId());
        pointsHistory.setRelatedType("order");
        pointsHistory.setBalanceAfter(user.getPoints() - totalPoints);
        pointsHistoryRepository.save(pointsHistory);
        
        // 清空购物车中已下单的商品
        for (Cart cartItem : selectedItems) {
            cartRepository.delete(cartItem);
        }
        
        return order;
    }
    
    public List<Order> getUserOrders(Long userId, String status) {
        if (status == null || status.isBlank()) {
            return orderRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        }
        String normalized = status.trim();
        if (!ALLOWED_LIST_STATUS_FILTER.contains(normalized)) {
            throw new IllegalArgumentException(
                    "无效的状态筛选参数，允许值：pending、processing、shipped、completed、cancelled");
        }
        return orderRepository.findByUserIdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(userId, normalized);
    }

    @Transactional
    public Order confirmReceipt(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (order.getDeletedAt() != null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if ("completed".equals(order.getStatus())) {
            return order;
        }
        if (!"shipped".equals(order.getStatus())) {
            throw new RuntimeException("只有已发货的订单可以确认收货");
        }

        order.setStatus("completed");
        return orderRepository.save(order);
    }
    
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待处理状态的订单");
        }
        
        // 返还积分
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userService.updatePoints(userId, order.getTotalPoints());
            
            // 记录积分历史
            PointsHistory pointsHistory = new PointsHistory();
            pointsHistory.setUserId(userId);
            pointsHistory.setPoints(order.getTotalPoints());
            pointsHistory.setType("refund");
            pointsHistory.setDescription("订单取消退款：" + order.getOrderNo());
            pointsHistory.setRelatedId(order.getId());
            pointsHistory.setRelatedType("order");
            pointsHistory.setBalanceAfter(user.getPoints() + order.getTotalPoints());
            pointsHistoryRepository.save(pointsHistory);
        }
        
        // 恢复库存
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSales(Math.max(0, product.getSales() - item.getQuantity()));
                productRepository.save(product);
            }
        }
        
        order.setStatus("cancelled");
        return orderRepository.save(order);
    }
}
