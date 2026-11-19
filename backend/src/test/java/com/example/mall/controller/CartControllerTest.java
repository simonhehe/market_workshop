package com.example.mall.controller;

import com.example.mall.common.BusinessException;
import com.example.mall.common.CartAccessDeniedException;
import com.example.mall.common.CartItemNotFoundException;
import com.example.mall.entity.Cart;
import com.example.mall.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest {

    private CartService cartService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CartController(cartService)).build();
    }

    @Test
    void updatesSelectionStateForCurrentUser() throws Exception {
        Cart cart = new Cart();
        cart.setId(5L);
        cart.setUserId(1L);
        cart.setIsSelected(0);
        when(cartService.selectItem(5L, 1L, 0)).thenReturn(cart);

        mockMvc.perform(put("/cart/5/select")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isSelected\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isSelected").value(0));

        verify(cartService).selectItem(5L, 1L, 0);
    }

    @Test
    void returnsCartItemForbiddenForAnotherUsersSelection() throws Exception {
        when(cartService.selectItem(5L, 1L, 1))
                .thenThrow(new CartAccessDeniedException("无权操作此购物车商品"));

        mockMvc.perform(put("/cart/5/select")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isSelected\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("CART_ITEM_FORBIDDEN"));
    }

    @Test
    void updatesQuantityForCurrentUser() throws Exception {
        Cart cart = new Cart();
        cart.setId(5L);
        cart.setQuantity(3);
        when(cartService.updateQuantity(5L, 1L, 3)).thenReturn(cart);

        mockMvc.perform(put("/cart/5")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(3));

        verify(cartService).updateQuantity(5L, 1L, 3);
    }

    @Test
    void removesCartItemForCurrentUser() throws Exception {
        mockMvc.perform(delete("/cart/5").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).removeFromCart(5L, 1L);
    }

    @Test
    void returnsStockErrorWhenAddingAboveStock() throws Exception {
        when(cartService.addToCart(1L, 3L, 2))
                .thenThrow(new BusinessException("STOCK_INSUFFICIENT", "库存不足"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/cart")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":3,\"quantity\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("STOCK_INSUFFICIENT"));
    }

    @Test
    void returnsBusinessErrorWhenRemovalIsForbidden() throws Exception {
        doThrow(new CartAccessDeniedException("无权操作此购物车商品"))
                .when(cartService).removeFromCart(5L, 1L);

        mockMvc.perform(delete("/cart/5").requestAttr("userId", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("CART_ITEM_FORBIDDEN"));
    }

    @Test
    void returnsCartItemsAndSummary() throws Exception {
        Cart cart = new Cart();
        cart.setId(5L);
        cart.setProductId(3L);
        cart.setQuantity(2);
        cart.setIsSelected(1);
        when(cartService.getCartByUserId(1L)).thenReturn(java.util.List.of(cart));
        when(cartService.getCartSummary(1L)).thenReturn(java.util.Map.of(
                "totalCount", 1,
                "selectedCount", 1,
                "totalPoints", 20,
                "totalQuantity", 2));

        mockMvc.perform(get("/cart").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartItems[0].productId").value(3))
                .andExpect(jsonPath("$.data.summary.selectedCount").value(1));
    }

    @Test
    void clearsCartForCurrentUser() throws Exception {
        mockMvc.perform(delete("/cart/clear").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).clearCart(1L);
    }

    @Test
    void defaultsQuantityToOneWhenOmitted() throws Exception {
        Cart cart = new Cart();
        cart.setId(5L);
        cart.setQuantity(1);
        when(cartService.addToCart(1L, 3L, 1)).thenReturn(cart);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/cart")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(1));
    }

    @Test
    void returnsNullDataWhenQuantityBecomesZero() throws Exception {
        when(cartService.updateQuantity(5L, 1L, 0)).thenReturn(null);

        mockMvc.perform(put("/cart/5")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("商品已从购物车移除"));
    }

    @Test
    void returnsCart001WhenAddPayloadIsInvalid() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/cart")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void returnsCart001WhenGetFailsUnexpectedly() throws Exception {
        when(cartService.getCartByUserId(1L)).thenThrow(new RuntimeException("database down"));

        mockMvc.perform(get("/cart").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void returnsCart001WhenUpdateFailsUnexpectedly() throws Exception {
        when(cartService.updateQuantity(5L, 1L, 3)).thenThrow(new RuntimeException("database down"));

        mockMvc.perform(put("/cart/5")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void returnsCart001WhenClearFailsUnexpectedly() throws Exception {
        doThrow(new RuntimeException("database down")).when(cartService).clearCart(1L);

        mockMvc.perform(delete("/cart/clear").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void returnsCart001WhenSelectFailsUnexpectedly() throws Exception {
        when(cartService.selectItem(5L, 1L, 1)).thenThrow(new RuntimeException("database down"));

        mockMvc.perform(put("/cart/5/select")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isSelected\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void returnsCart001WhenRemoveFailsUnexpectedly() throws Exception {
        doThrow(new RuntimeException("database down")).when(cartService).removeFromCart(5L, 1L);

        mockMvc.perform(delete("/cart/5").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }

    @Test
    void returnsBusinessCodeWhenCartLoadFails() throws Exception {
        when(cartService.getCartByUserId(1L))
                .thenThrow(new BusinessException("CART_ITEM_FORBIDDEN", "无权操作此购物车商品"));

        mockMvc.perform(get("/cart").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_ITEM_FORBIDDEN"));
    }

    @Test
    void returnsNotFoundWhenSelectingMissingCartItem() throws Exception {
        when(cartService.selectItem(404L, 1L, 1))
                .thenThrow(new CartItemNotFoundException("购物车项不存在"));

        mockMvc.perform(put("/cart/404/select")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isSelected\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CART_404"));
    }

    @Test
    void returnsStockErrorWhenUpdatingAboveStock() throws Exception {
        when(cartService.updateQuantity(5L, 1L, 99))
                .thenThrow(new BusinessException("STOCK_INSUFFICIENT", "库存不足"));

        mockMvc.perform(put("/cart/5")
                        .requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("STOCK_INSUFFICIENT"));
    }

    @Test
    void returnsBusinessErrorWhenRemovalFailsForBusinessReason() throws Exception {
        doThrow(new BusinessException("CART_ITEM_FORBIDDEN", "无权操作此购物车商品"))
                .when(cartService).removeFromCart(5L, 1L);

        mockMvc.perform(delete("/cart/5").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_ITEM_FORBIDDEN"));
    }

    @Test
    void returnsBusinessErrorWhenClearingFails() throws Exception {
        doThrow(new BusinessException("CART_001", "清空购物车失败"))
                .when(cartService).clearCart(1L);

        mockMvc.perform(delete("/cart/clear").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"));
    }
}
