package com.example.mall.service;

import com.example.mall.entity.Order;
import com.example.mall.repository.CategoryRepository;
import com.example.mall.repository.OrderRepository;
import com.example.mall.repository.ProductRepository;
import com.example.mall.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * WB-A-01：管理端 {@code updateOrderStatus} 当前无状态机校验，任意字符串可写入（矩阵 §2 / §4 现状固化）。
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void updateOrderStatus_acceptsArbitraryStatusString() {
        Order order = new Order();
        order.setId(9L);
        order.setStatus("pending");
        when(orderRepository.findById(9L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order out = adminService.updateOrderStatus(9L, "weird-custom-status");

        assertEquals("weird-custom-status", out.getStatus());
    }
}
