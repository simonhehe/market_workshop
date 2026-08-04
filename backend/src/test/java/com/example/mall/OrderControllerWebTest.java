package com.example.mall;

import com.example.mall.controller.OrderController;
import com.example.mall.entity.Order;
import com.example.mall.entity.OrderItem;
import com.example.mall.repository.OrderItemRepository;
import com.example.mall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerWebTest {

    @Mock OrderService orderService;
    @Mock OrderItemRepository orderItemRepository;
    @InjectMocks OrderController orderController;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    @Test
    void getUserOrders_success() throws Exception {
        when(orderService.getUserOrders(1L)).thenReturn(List.of(new Order()));

        mockMvc.perform(get("/orders").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUserOrders_serviceError() throws Exception {
        when(orderService.getUserOrders(1L)).thenThrow(new RuntimeException("orders fail"));

        mockMvc.perform(get("/orders").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_002"));
    }

    @Test
    void getOrderDetail_success() throws Exception {
        Order order = new Order();
        order.setId(9L);
        order.setUserId(1L);
        when(orderService.getOrderById(9L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(9L)).thenReturn(List.of(new OrderItem()));

        mockMvc.perform(get("/orders/9").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order.id").value(9));
    }

    @Test
    void getOrderDetail_notFound() throws Exception {
        when(orderService.getOrderById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/9").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("订单不存在"));
    }

    @Test
    void getOrderDetail_foreignUser() throws Exception {
        Order order = new Order();
        order.setId(9L);
        order.setUserId(99L);
        when(orderService.getOrderById(9L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/9").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("无权查看此订单"));
    }

    @Test
    void createOrder_serviceError() throws Exception {
        when(orderService.createOrder(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("create fail"));

        mockMvc.perform(post("/orders").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_001"));
    }

    @Test
    void getOrderDetail_serviceError() throws Exception {
        when(orderService.getOrderById(9L)).thenThrow(new RuntimeException("detail fail"));

        mockMvc.perform(get("/orders/9").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("detail fail"));
    }

    @Test
    void createOrder_withRemark() throws Exception {
        Order order = new Order();
        order.setId(1L);
        when(orderService.createOrder(eq(1L), eq(2L), eq("note"))).thenReturn(order);

        mockMvc.perform(post("/orders").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":2,\"remark\":\"note\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void cancelOrder_serviceError() throws Exception {
        when(orderService.cancelOrder(3L, 1L)).thenThrow(new RuntimeException("cancel fail"));

        mockMvc.perform(put("/orders/3/cancel").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_002"));
    }
}
