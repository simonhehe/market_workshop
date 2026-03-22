package com.example.mall.controller;

import com.example.mall.entity.Order;
import com.example.mall.repository.OrderItemRepository;
import com.example.mall.service.OrderService;
import com.example.mall.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderItemRepository orderItemRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void getUserOrders_forwardsStatusQuery() throws Exception {
        when(orderService.getUserOrders(eq(1L), eq("pending"))).thenReturn(List.of());

        mockMvc.perform(get("/orders")
                        .param("status", "pending")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(orderService).getUserOrders(1L, "pending");
    }

    @Test
    void createOrder_acceptsJsonBody() throws Exception {
        Order created = new Order();
        created.setId(100L);
        when(orderService.createOrder(eq(1L), eq(2L), eq("r"))).thenReturn(created);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":2,\"remark\":\"r\"}")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100));

        verify(orderService).createOrder(1L, 2L, "r");
    }

    /** WB-API-03 */
    @Test
    void getOrderDetail_forbiddenWhenNotOwner() throws Exception {
        Order otherUsers = new Order();
        otherUsers.setId(5L);
        otherUsers.setUserId(2L);
        when(orderService.getOrderById(5L)).thenReturn(Optional.of(otherUsers));

        mockMvc.perform(get("/orders/5")
                        .requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(orderItemRepository, never()).findByOrderId(anyLong());
    }
}
