package com.example.mall;

import com.example.mall.entity.Cart;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.repository.CartRepository;
import com.example.mall.repository.ProductRepository;
import com.example.mall.repository.UserRepository;
import com.example.mall.support.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * cart-selection capability — TDD retroactive coverage (spec: openspec cart-selection).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartSelectionIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataHelper helper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired ObjectMapper objectMapper;

    User user;
    Product product;
    Cart cartItem;
    String token;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = helper.createUser("cartuser", 100);
        product = helper.createProduct("勾选测试商品", 30, 5);
        cartItem = helper.addCartItem(user.getId(), product.getId(), 2, 1);
        token = helper.bearerToken(user);
    }

    @Test
    void selectCartItem_deselect_excludesFromSummary() throws Exception {
        mockMvc.perform(put("/cart/" + cartItem.getId() + "/select")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"isSelected\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isSelected").value(0));

        Cart persisted = cartRepository.findById(cartItem.getId()).orElseThrow();
        assertEquals(0, persisted.getIsSelected());

        mockMvc.perform(get("/cart").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.selectedCount").value(0))
                .andExpect(jsonPath("$.data.summary.totalPoints").value(0));
    }

    @Test
    void selectCartItem_select_includesInSummary() throws Exception {
        cartItem.setIsSelected(0);
        cartRepository.save(cartItem);

        mockMvc.perform(put("/cart/" + cartItem.getId() + "/select")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"isSelected\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isSelected").value(1));

        mockMvc.perform(get("/cart").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.selectedCount").value(1))
                .andExpect(jsonPath("$.data.summary.totalPoints").value(60));
    }

    @Test
    void selectCartItem_rejectsForeignCart() throws Exception {
        User other = helper.createUser("other", 100);
        Cart foreignCart = helper.addCartItem(other.getId(), product.getId(), 1, 1);

        mockMvc.perform(put("/cart/" + foreignCart.getId() + "/select")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"isSelected\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"))
                .andExpect(jsonPath("$.message", containsString("无权")));
    }

    @Test
    void selectCartItem_rejectsMissingCart() throws Exception {
        mockMvc.perform(put("/cart/99999/select")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"isSelected\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CART_001"))
                .andExpect(jsonPath("$.message", containsString("不存在")));
    }
}
