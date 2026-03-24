package com.example.mall.controller;

import com.example.mall.entity.Order;
import com.example.mall.middleware.AuthMiddleware;
import com.example.mall.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerOrderTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthMiddleware authMiddleware;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("GET /admin/orders: 非 admin 返回 403 AUTH_004（权限校验）")
    void getAllOrders_forbiddenWhenNotAdmin() throws Exception {
        mockMvc.perform(get("/admin/orders")
                        .requestAttr("role", "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_004"));
    }

    @Test
    @DisplayName("GET /admin/orders: admin 成功")
    void getAllOrders_success() throws Exception {
        when(adminService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/admin/orders")
                        .requestAttr("role", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(adminService).getAllOrders();
    }

    @Test
    @DisplayName("PUT /admin/orders/{id}/status: 非 admin 403")
    void updateOrderStatus_forbiddenWhenNotAdmin() throws Exception {
        mockMvc.perform(put("/admin/orders/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"shipped\"}")
                        .requestAttr("role", "user"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_004"));
    }

    @Test
    @DisplayName("PUT /admin/orders/{id}/status: admin 调 service 更新状态")
    void updateOrderStatus_success() throws Exception {
        Order updated = new Order();
        updated.setId(1L);
        updated.setStatus("shipped");
        when(adminService.updateOrderStatus(eq(1L), eq("shipped"))).thenReturn(updated);

        mockMvc.perform(put("/admin/orders/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"shipped\"}")
                        .requestAttr("role", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("shipped"));

        verify(adminService).updateOrderStatus(1L, "shipped");
    }
}
