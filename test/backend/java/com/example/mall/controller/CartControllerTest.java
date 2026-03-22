package com.example.mall.controller;

import com.example.mall.entity.Cart;
import com.example.mall.service.CartService;
import com.example.mall.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    /** 满足 WebConfig → AuthMiddleware 对 JwtUtil 的依赖，避免切片测试拉起完整安全链失败 */
    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void updateSelectionPost_callsService() throws Exception {
        Cart cart = new Cart();
        cart.setId(5L);
        when(cartService.updateSelection(eq(5L), eq(1L), eq(0))).thenReturn(cart);

        mockMvc.perform(post("/cart/selection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cartItemId\":5,\"isSelected\":0}")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).updateSelection(5L, 1L, 0);
    }

    @Test
    void updateSelectionPut_stillWorks() throws Exception {
        Cart cart = new Cart();
        cart.setId(5L);
        when(cartService.updateSelection(eq(5L), eq(1L), eq(1))).thenReturn(cart);

        mockMvc.perform(put("/cart/5/selection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isSelected\":1}")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).updateSelection(5L, 1L, 1);
    }

    @Test
    void updateQuantity_passesUserId() throws Exception {
        Cart cart = new Cart();
        when(cartService.updateQuantity(eq(3L), eq(2L), eq(4))).thenReturn(cart);

        mockMvc.perform(put("/cart/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":4}")
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk());

        verify(cartService).updateQuantity(3L, 2L, 4);
    }
}
