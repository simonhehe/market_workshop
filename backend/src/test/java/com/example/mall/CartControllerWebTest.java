package com.example.mall;

import com.example.mall.controller.CartController;
import com.example.mall.entity.Cart;
import com.example.mall.service.CartService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerWebTest {

    @Mock CartService cartService;
    @InjectMocks CartController cartController;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    void getCart_success() throws Exception {
        when(cartService.getCartByUserId(1L)).thenReturn(List.of(new Cart()));
        when(cartService.getCartSummary(1L)).thenReturn(Map.of("totalPoints", 10));

        mockMvc.perform(get("/cart").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getCart_serviceError() throws Exception {
        when(cartService.getCartByUserId(1L)).thenThrow(new RuntimeException("cart fail"));

        mockMvc.perform(get("/cart").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void addToCart_defaultQuantity() throws Exception {
        Cart cart = new Cart();
        cart.setQuantity(1);
        when(cartService.addToCart(eq(1L), eq(2L), eq(1))).thenReturn(cart);

        mockMvc.perform(post("/cart").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(1));
    }

    @Test
    void addToCart_serviceError() throws Exception {
        when(cartService.addToCart(anyLong(), anyLong(), anyInt()))
                .thenThrow(new RuntimeException("add fail"));

        mockMvc.perform(post("/cart").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":2,\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("add fail"));
    }

    @Test
    void updateQuantity_success() throws Exception {
        Cart cart = new Cart();
        cart.setQuantity(3);
        when(cartService.updateQuantity(5L, 3)).thenReturn(cart);

        mockMvc.perform(put("/cart/5").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(3));
    }

    @Test
    void updateQuantity_removed() throws Exception {
        when(cartService.updateQuantity(5L, 0)).thenReturn(null);

        mockMvc.perform(put("/cart/5").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("商品已从购物车移除"));
    }

    @Test
    void updateQuantity_serviceError() throws Exception {
        when(cartService.updateQuantity(anyLong(), anyInt()))
                .thenThrow(new RuntimeException("update fail"));

        mockMvc.perform(put("/cart/5").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void removeFromCart_success() throws Exception {
        mockMvc.perform(delete("/cart/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void removeFromCart_serviceError() throws Exception {
        doThrow(new RuntimeException("remove fail")).when(cartService).removeFromCart(9L);

        mockMvc.perform(delete("/cart/9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("remove fail"));
    }

    @Test
    void clearCart_success() throws Exception {
        mockMvc.perform(delete("/cart/clear").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void clearCart_serviceError() throws Exception {
        doThrow(new RuntimeException("clear fail")).when(cartService).clearCart(1L);

        mockMvc.perform(delete("/cart/clear").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void selectCartItem_serviceError() throws Exception {
        when(cartService.selectCartItem(anyLong(), anyLong(), anyInt()))
                .thenThrow(new RuntimeException("select fail"));

        mockMvc.perform(put("/cart/1/select").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isSelected\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("select fail"));
    }
}
