package com.example.mall.service;

import com.example.mall.entity.Order;
import com.example.mall.repository.CategoryRepository;
import com.example.mall.repository.OrderRepository;
import com.example.mall.repository.ProductRepository;
import com.example.mall.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    @DisplayName("updateOrderStatus: 订单不存在则失败")
    void updateOrderStatus_failsWhenMissing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateOrderStatus(1L, "shipped"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("订单不存在");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateOrderStatus: 无状态机校验，任意字符串直接落库（与用户取消语义不同）")
    void updateOrderStatus_setsStatusWithoutStateMachine() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("pending");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order updated = adminService.updateOrderStatus(1L, "cancelled");

        assertThat(updated.getStatus()).isEqualTo("cancelled");
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("updateOrderStatus: 可写入 shipped / completed 等业务状态")
    void updateOrderStatus_allowsForwardStatuses() {
        Order order = new Order();
        order.setId(2L);
        order.setStatus("pending");
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(adminService.updateOrderStatus(2L, "completed").getStatus()).isEqualTo("completed");
    }
}
