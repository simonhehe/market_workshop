package com.example.mall.controller;

import com.example.mall.entity.Order;
import com.example.mall.middleware.AuthMiddleware;
import com.example.mall.repository.OrderItemRepository;
import com.example.mall.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** 避免 WebConfig / FilterConfig 拉起真实 AuthMiddleware → JwtUtil 不在 WebMvc 切片内 */
    @MockBean
    private AuthMiddleware authMiddleware;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderItemRepository orderItemRepository;

    @Test
    @DisplayName("POST /orders: 成功返回 ORDER_001 成功体（依赖 request userId）")
    void createOrder_success() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("pending");
        when(orderService.createOrder(eq(10L), eq(null), eq(null))).thenReturn(order);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .requestAttr("userId", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(orderService).createOrder(10L, null, null);
    }

    @Test
    @DisplayName("GET /orders: 无参列表")
    void getUserOrders_noParam() throws Exception {
        when(orderService.getUserOrders(10L, null)).thenReturn(List.of());

        mockMvc.perform(get("/orders").requestAttr("userId", 10L))
                .andExpect(status().isOk());

        verify(orderService).getUserOrders(10L, null);
    }

    @Test
    @DisplayName("GET /orders: status 筛选")
    void getUserOrders_withStatusParam() throws Exception {
        when(orderService.getUserOrders(10L, "shipped")).thenReturn(List.of());

        mockMvc.perform(get("/orders")
                        .param("status", "shipped")
                        .requestAttr("userId", 10L))
                .andExpect(status().isOk());

        verify(orderService).getUserOrders(10L, "shipped");
    }

    @Test
    @DisplayName("GET /orders: 非法 status → ORDER_004")
    void getUserOrders_invalidStatus_returnsOrder004() throws Exception {
        when(orderService.getUserOrders(10L, "bad"))
                .thenThrow(new IllegalArgumentException("无效的状态筛选参数"));

        mockMvc.perform(get("/orders")
                        .param("status", "bad")
                        .requestAttr("userId", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_004"));
    }

    @Test
    @DisplayName("POST /orders: 业务异常映射为 ORDER_001")
    void createOrder_mapsExceptionToOrder001() throws Exception {
        when(orderService.createOrder(any(), any(), any()))
                .thenThrow(new RuntimeException("积分不足"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":1}")
                        .requestAttr("userId", 5L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ORDER_001"));
    }

    @Test
    @DisplayName("GET /orders/{id}: 订单不存在 → ORDER_002")
    void getOrderDetail_notFound() throws Exception {
        when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/{id}", 99L).requestAttr("userId", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_002"));
    }

    @Test
    @DisplayName("GET /orders/{id}: 非本人返回 ORDER_002（归属校验）")
    void getOrderDetail_forbiddenForOtherUser() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(99L);
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/{id}", 1L)
                        .requestAttr("userId", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_002"));
    }

    @Test
    @DisplayName("GET /orders/{id}: 本人可查看明细")
    void getOrderDetail_success() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/orders/{id}", 1L)
                        .requestAttr("userId", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order.id").value(1));
    }

    @Test
    @DisplayName("PUT /orders/{id}/cancel: 异常映射 ORDER_002")
    void cancelOrder_mapsException() throws Exception {
        when(orderService.cancelOrder(1L, 10L))
                .thenThrow(new RuntimeException("只能取消待处理状态的订单"));

        mockMvc.perform(put("/orders/{id}/cancel", 1L)
                        .requestAttr("userId", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_002"));
    }

    @Test
    @DisplayName("PUT /orders/{id}/cancel: 成功")
    void cancelOrder_success() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("cancelled");
        when(orderService.cancelOrder(1L, 10L)).thenReturn(order);

        mockMvc.perform(put("/orders/{id}/cancel", 1L)
                        .requestAttr("userId", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("cancelled"));
    }

    @Test
    @DisplayName("PUT /orders/{id}/confirm-receipt: 成功")
    void confirmReceipt_success() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("completed");
        when(orderService.confirmReceipt(1L, 10L)).thenReturn(order);

        mockMvc.perform(put("/orders/{id}/confirm-receipt", 1L)
                        .requestAttr("userId", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("completed"));
    }

    @Test
    @DisplayName("PUT /orders/{id}/confirm-receipt: 业务异常 → ORDER_003")
    void confirmReceipt_mapsExceptionToOrder003() throws Exception {
        when(orderService.confirmReceipt(1L, 10L))
                .thenThrow(new RuntimeException("只有已发货的订单可以确认收货"));

        mockMvc.perform(put("/orders/{id}/confirm-receipt", 1L)
                        .requestAttr("userId", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_003"));
    }
}
