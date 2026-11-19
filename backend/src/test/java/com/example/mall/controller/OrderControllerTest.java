package com.example.mall.controller;

import com.example.mall.common.BusinessException;
import com.example.mall.entity.Order;
import com.example.mall.repository.OrderItemRepository;
import com.example.mall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private OrderService orderService;
    private OrderItemRepository orderItemRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        orderItemRepository = mock(OrderItemRepository.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrderController(orderService, orderItemRepository))
                .build();
    }

    @Test
    void createsOrderAndIgnoresClientProvidedTotalsAndStatus() throws Exception {
        Order order = new Order();
        order.setId(9L);
        order.setOrderNo("ORD20261120001");
        order.setTotalPoints(200);
        order.setStatus("pending");
        when(orderService.createOrder(1L, 7L, "备注")).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":7,\"remark\":\"备注\",\"totalPoints\":1,\"status\":\"completed\",\"points\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNo").value("ORD20261120001"))
                .andExpect(jsonPath("$.data.totalPoints").value(200))
                .andExpect(jsonPath("$.data.status").value("pending"));

        verify(orderService).createOrder(1L, 7L, "备注");
    }

    @Test
    void returnsBusinessErrorCodeWhenStockIsInsufficient() throws Exception {
        when(orderService.createOrder(eq(1L), eq(7L), any()))
                .thenThrow(new BusinessException("STOCK_INSUFFICIENT", "库存不足：商品「积分水杯」仅剩 1 件，需要 2 件"));

        mockMvc.perform(post("/orders")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("STOCK_INSUFFICIENT"));
    }

    @Test
    void returnsAddressRequiredWhenAddressIdIsMissing() throws Exception {
        when(orderService.createOrder(1L, null, null))
                .thenThrow(new BusinessException("ADDR_REQUIRED", "请选择收货地址后再提交订单"));

        mockMvc.perform(post("/orders")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ADDR_REQUIRED"));
    }

    @Test
    void returnsOrderForbiddenWhenCancellingAnotherUsersOrder() throws Exception {
        when(orderService.cancelOrder(9L, 1L))
                .thenThrow(new BusinessException("ORDER_FORBIDDEN", "无权操作此订单"));

        mockMvc.perform(put("/orders/9/cancel").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_FORBIDDEN"));
    }

    @Test
    void listsOrdersForCurrentUser() throws Exception {
        Order order = new Order();
        order.setId(9L);
        order.setOrderNo("ORD-9");
        order.setStatus("pending");
        order.setTotalPoints(100);
        when(orderService.getUserOrders(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderNo").value("ORD-9"));
    }

    @Test
    void returnsOrderForbiddenWhenViewingAnotherUsersOrder() throws Exception {
        Order order = new Order();
        order.setId(9L);
        order.setUserId(2L);
        when(orderService.getOrderById(9L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/9").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_FORBIDDEN"));
    }

    @Test
    void returnsOrderNotFoundWhenDetailIsMissing() throws Exception {
        when(orderService.getOrderById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/9").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void cancelsOwnPendingOrder() throws Exception {
        Order order = new Order();
        order.setId(9L);
        order.setStatus("cancelled");
        when(orderService.cancelOrder(9L, 1L)).thenReturn(order);

        mockMvc.perform(put("/orders/9/cancel").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("cancelled"));
    }

    @Test
    void returnsOrderDetailWithItemsForOwner() throws Exception {
        Order order = new Order();
        order.setId(9L);
        order.setUserId(1L);
        when(orderService.getOrderById(9L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(9L)).thenReturn(List.of());

        mockMvc.perform(get("/orders/9").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.order.id").value(9))
                .andExpect(jsonPath("$.data.orderItems").isArray());
    }

    @Test
    void returnsOrder001WhenCreateFailsUnexpectedly() throws Exception {
        when(orderService.createOrder(1L, 7L, null))
                .thenThrow(new RuntimeException("database down"));

        mockMvc.perform(post("/orders")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_001"));
    }

    @Test
    void returnsOrder002WhenListFailsUnexpectedly() throws Exception {
        when(orderService.getUserOrders(1L)).thenThrow(new RuntimeException("database down"));

        mockMvc.perform(get("/orders").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_002"));
    }

    @Test
    void returnsOrder002WhenCancelFailsUnexpectedly() throws Exception {
        when(orderService.cancelOrder(9L, 1L)).thenThrow(new RuntimeException("database down"));

        mockMvc.perform(put("/orders/9/cancel").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_002"));
    }

    @Test
    void returnsOrder002WhenDetailFailsUnexpectedly() throws Exception {
        when(orderService.getOrderById(9L)).thenThrow(new RuntimeException("database down"));

        mockMvc.perform(get("/orders/9").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_002"));
    }
}
