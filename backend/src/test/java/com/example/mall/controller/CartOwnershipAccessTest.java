package com.example.mall.controller;

import com.example.mall.entity.Cart;
import com.example.mall.repository.CartRepository;
import com.example.mall.support.TestDatabase;
import com.example.mall.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartOwnershipAccessTest {

    private static final String DB_URL = TestDatabase.newUniqueSqliteUrl();
    private static final long USER_A = 1001L;
    private static final long USER_B = 1002L;

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DB_URL);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Long ownCartId;
    private Long otherCartId;
    private String tokenA;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        ownCartId = cartRepository.save(cart(USER_A, 1L, 2)).getId();
        otherCartId = cartRepository.save(cart(USER_B, 1L, 5)).getId();
        tokenA = jwtUtil.generateToken(USER_A, "userA", "user");
    }

    @Test
    void updateQuantity_updatesOwnCart() throws Exception {
        mockMvc.perform(put("/cart/" + ownCartId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantity").value(3));

        assertEquals(3, cartRepository.findById(ownCartId).orElseThrow().getQuantity());
    }

    @Test
    void updateQuantity_rejectsOtherUsersCart() throws Exception {
        mockMvc.perform(put("/cart/" + otherCartId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(APPLICATION_JSON)
                        .content("{\"quantity\":9}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CART_ITEM_FORBIDDEN"));

        Cart untouched = cartRepository.findById(otherCartId).orElseThrow();
        assertEquals(5, untouched.getQuantity());
        assertEquals(USER_B, untouched.getUserId());
    }

    @Test
    void updateQuantity_returnsNotFoundForMissingCart() throws Exception {
        mockMvc.perform(put("/cart/999999")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CART_404"));
    }

    @Test
    void deleteCart_removesOwnCart() throws Exception {
        mockMvc.perform(delete("/cart/" + ownCartId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(cartRepository.findById(ownCartId).isPresent());
    }

    @Test
    void deleteCart_rejectsOtherUsersCart() throws Exception {
        mockMvc.perform(delete("/cart/" + otherCartId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CART_ITEM_FORBIDDEN"));

        assertTrue(cartRepository.findById(otherCartId).isPresent());
    }

    @Test
    void deleteCart_returnsNotFoundForMissingCart() throws Exception {
        mockMvc.perform(delete("/cart/999999")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CART_404"));
    }

    @Test
    void updateQuantity_requiresAuthentication() throws Exception {
        mockMvc.perform(put("/cart/" + ownCartId)
                        .contentType(APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_001"));

        assertEquals(2, cartRepository.findById(ownCartId).orElseThrow().getQuantity());
    }

    @Test
    void deleteCart_requiresAuthentication() throws Exception {
        mockMvc.perform(delete("/cart/" + ownCartId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_001"));

        assertTrue(cartRepository.findById(ownCartId).isPresent());
    }

    private Cart cart(long userId, long productId, int quantity) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setIsSelected(1);
        return cart;
    }
}
