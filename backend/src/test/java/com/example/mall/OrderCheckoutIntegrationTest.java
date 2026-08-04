package com.example.mall;

import com.example.mall.entity.Address;
import com.example.mall.entity.Order;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.repository.AddressRepository;
import com.example.mall.repository.CartRepository;
import com.example.mall.repository.OrderRepository;
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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderCheckoutIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataHelper helper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired AddressRepository addressRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired ObjectMapper objectMapper;

    User user;
    Product product;
    Address address;
    String token;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = helper.createUser("t1user", 500);
        product = helper.createProduct("测试商品", 50, 10);
        helper.addCartItem(user.getId(), product.getId(), 2, 1);
        address = helper.createAddress(user.getId(), true);
        token = helper.bearerToken(user);
    }

    @Test
    void contextLoads() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void createOrder_success_pending() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("addressId", address.getId());
        body.put("remark", "测试备注");

        mockMvc.perform(post("/orders")
                        .header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.totalPoints").value(100));

        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(400, refreshed.getPoints());
        Product refreshedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(8, refreshedProduct.getStock());
        assertEquals(0, cartRepository.findByUserId(user.getId()).size());
    }

    @Test
    void createOrder_noSelectedItems() throws Exception {
        cartRepository.deleteAll();
        helper.addCartItem(user.getId(), product.getId(), 1, 0);
        Map<String, Object> body = Map.of("addressId", address.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_001"))
                .andExpect(jsonPath("$.message", containsString("选中的商品")));
    }

    @Test
    void createOrder_insufficientStock() throws Exception {
        product.setStock(1);
        productRepository.save(product);
        Map<String, Object> body = Map.of("addressId", address.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_001"))
                .andExpect(jsonPath("$.message", containsString("库存不足")));
    }

    @Test
    void createOrder_insufficientPoints() throws Exception {
        user.setPoints(10);
        userRepository.save(user);
        Map<String, Object> body = Map.of("addressId", address.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_001"))
                .andExpect(jsonPath("$.message", containsString("积分不足")));
    }

    @Test
    void createOrder_missingAddressId() throws Exception {
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_001"))
                .andExpect(jsonPath("$.message").value("请选择收货地址"));
    }

    @Test
    void createOrder_emptyAddressIdString() throws Exception {
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"addressId\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请选择收货地址"));
    }

    @Test
    void createOrder_invalidAddressIdFormat() throws Exception {
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content("{\"addressId\":\"not-a-number\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ORDER_001"))
                .andExpect(jsonPath("$.message").value("请选择收货地址"));
    }

    @Test
    void createOrder_foreignAddress() throws Exception {
        User other = helper.createUser("other", 100);
        Address foreign = helper.createAddress(other.getId(), true);
        Map<String, Object> body = Map.of("addressId", foreign.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("收货地址不存在"));
    }

    @Test
    void cancelOrder_pending_success() throws Exception {
        Map<String, Object> body = Map.of("addressId", address.getId());
        String createJson = mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createJson).path("data").path("id").asLong();

        mockMvc.perform(put("/orders/" + orderId + "/cancel").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("cancelled"));

        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(500, refreshed.getPoints());
    }

    @Test
    void createOrder_addressNotFound() throws Exception {
        Map<String, Object> body = Map.of("addressId", 99999L);
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("收货地址不存在"));
    }

    @Test
    void getUserOrders_success() throws Exception {
        Map<String, Object> body = Map.of("addressId", address.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/orders")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getOrderDetail_success() throws Exception {
        Map<String, Object> body = Map.of("addressId", address.getId());
        String createJson = mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createJson).path("data").path("id").asLong();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/orders/" + orderId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order.id").value(orderId));
    }

    @Test
    void cancelOrder_rejectNonPending() throws Exception {
        Map<String, Object> body = Map.of("addressId", address.getId());
        String createJson = mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createJson).path("data").path("id").asLong();
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus("shipped");
        orderRepository.save(order);

        mockMvc.perform(put("/orders/" + orderId + "/cancel").header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("待处理")));
    }

    @Test
    void getOrderDetail_notFound() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/orders/99999")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("订单不存在"));
    }

    @Test
    void getOrderDetail_foreignUser() throws Exception {
        Map<String, Object> body = Map.of("addressId", address.getId());
        String createJson = mockMvc.perform(post("/orders").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createJson).path("data").path("id").asLong();

        User other = helper.createUser("peeker", 100);
        String otherToken = helper.bearerToken(other);
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/orders/" + orderId)
                        .header("Authorization", otherToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("无权查看此订单"));
    }
}
