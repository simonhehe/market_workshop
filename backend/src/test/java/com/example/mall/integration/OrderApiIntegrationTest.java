package com.example.mall.integration;

import com.example.mall.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 全应用上下文 + 真实 {@code AuthMiddleware} + JWT（见 {@code tasks.md} §6.3）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void getOrders_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("AUTH_001")));
    }

    @Test
    void getOrderDetail_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelOrder_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/orders/1/cancel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void confirmReceipt_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/orders/1/confirm-receipt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminOrders_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminPutOrderStatus_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/admin/orders/1/status").contentType("application/json").content("{\"status\":\"shipped\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrders_withUserToken_emptyList() throws Exception {
        String token = jwtUtil.generateToken(1L, "u1", "user");
        mockMvc.perform(get("/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getOrders_withUserToken_invalidStatus_returnsOrder004() throws Exception {
        String token = jwtUtil.generateToken(1L, "u1", "user");
        mockMvc.perform(get("/orders").param("status", "nope").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_004"));
    }

    @Test
    void createOrder_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/orders").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAdminOrders_withUserToken_returns403() throws Exception {
        String token = jwtUtil.generateToken(1L, "u1", "user");
        mockMvc.perform(get("/admin/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_004"));
    }
}
