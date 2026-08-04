package com.example.mall;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartCrudIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataHelper helper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired ObjectMapper objectMapper;

    User user;
    Product product;
    String token;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        user = helper.createUser("cartcrud", 100);
        product = helper.createProduct("CRUD商品", 20, 10);
        token = helper.bearerToken(user);
    }

    @Test
    void addToCart_newItem() throws Exception {
        mockMvc.perform(post("/cart").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(2));
    }

    @Test
    void addToCart_rejectsMissingProduct() throws Exception {
        mockMvc.perform(post("/cart").header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"productId\":99999,\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("不存在")));
    }

    @Test
    void updateQuantity_zeroRemovesItem() throws Exception {
        var cart = helper.addCartItem(user.getId(), product.getId(), 2, 1);
        mockMvc.perform(put("/cart/" + cart.getId()).header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"quantity\":0}"))
                .andExpect(status().isOk());
        assertEquals(0, cartRepository.findByUserId(user.getId()).size());
    }

    @Test
    void removeFromCart_success() throws Exception {
        var cart = helper.addCartItem(user.getId(), product.getId(), 1, 1);
        mockMvc.perform(delete("/cart/" + cart.getId()).header("Authorization", token))
                .andExpect(status().isOk());
        assertEquals(0, cartRepository.findByUserId(user.getId()).size());
    }

    @Test
    void clearCart_success() throws Exception {
        helper.addCartItem(user.getId(), product.getId(), 1, 1);
        mockMvc.perform(delete("/cart/clear").header("Authorization", token))
                .andExpect(status().isOk());
        assertEquals(0, cartRepository.findByUserId(user.getId()).size());
    }

    @Test
    void getCart_returnsItemsAndSummary() throws Exception {
        helper.addCartItem(user.getId(), product.getId(), 2, 1);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/cart")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartItems", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.summary.totalPoints").value(40));
    }

    @Test
    void addToCart_mergesQuantity() throws Exception {
        helper.addCartItem(user.getId(), product.getId(), 1, 1);
        mockMvc.perform(post("/cart").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(3));
    }

    @Test
    void addToCart_insufficientStock() throws Exception {
        mockMvc.perform(post("/cart").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 99))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("库存不足")));
    }

    @Test
    void updateQuantity_increasesQuantity() throws Exception {
        var cart = helper.addCartItem(user.getId(), product.getId(), 1, 1);
        mockMvc.perform(put("/cart/" + cart.getId()).header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"quantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.quantity").value(4));
    }

    @Test
    void selectCartItem_toggleOff() throws Exception {
        var cart = helper.addCartItem(user.getId(), product.getId(), 1, 1);
        mockMvc.perform(put("/cart/" + cart.getId() + "/select").header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"isSelected\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isSelected").value(0));
    }

    @Test
    void selectCartItem_rejectsInvalidFlag() throws Exception {
        var cart = helper.addCartItem(user.getId(), product.getId(), 1, 1);
        mockMvc.perform(put("/cart/" + cart.getId() + "/select").header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"isSelected\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("无效")));
    }
}
