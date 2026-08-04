package com.example.mall;

import com.example.mall.entity.Address;
import com.example.mall.entity.User;
import com.example.mall.repository.AddressRepository;
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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AddressCheckoutIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataHelper helper;
    @Autowired UserRepository userRepository;
    @Autowired AddressRepository addressRepository;
    @Autowired ObjectMapper objectMapper;

    User user;
    String token;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        userRepository.deleteAll();
        user = helper.createUser("addruser", 100);
        token = helper.bearerToken(user);
    }

    @Test
    void getAddresses_emptyList() throws Exception {
        mockMvc.perform(get("/addresses").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void createAddress_success_forCheckoutSeed() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("receiver", "张三");
        body.put("phone", "13800138001");
        body.put("province", "北京市");
        body.put("city", "北京市");
        body.put("district", "朝阳区");
        body.put("detailAddress", "测试路1号");
        body.put("isDefault", 1);

        mockMvc.perform(post("/addresses").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.receiver").value("张三"));

        mockMvc.perform(get("/addresses").header("Authorization", token))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void updateAddress_success() throws Exception {
        Map<String, Object> createBody = new HashMap<>();
        createBody.put("receiver", "李四");
        createBody.put("phone", "13900139001");
        createBody.put("province", "上海市");
        createBody.put("city", "上海市");
        createBody.put("district", "浦东新区");
        createBody.put("detailAddress", "测试路2号");
        createBody.put("isDefault", 0);

        String createJson = mockMvc.perform(post("/addresses").header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long addressId = objectMapper.readTree(createJson).path("data").path("id").asLong();

        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("receiver", "王五");
        updateBody.put("phone", "13700137001");
        updateBody.put("detailAddress", "更新地址");

        mockMvc.perform(put("/addresses/" + addressId).header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiver").value("王五"))
                .andExpect(jsonPath("$.data.detailAddress").value("更新地址"));
    }

    @Test
    void deleteAddress_success() throws Exception {
        Address saved = helper.createAddress(user.getId(), false);
        mockMvc.perform(delete("/addresses/" + saved.getId()).header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/addresses").header("Authorization", token))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void deleteAddress_rejectsForeignUser() throws Exception {
        User other = helper.createUser("otheraddr", 50);
        Address foreign = helper.createAddress(other.getId(), true);
        mockMvc.perform(delete("/addresses/" + foreign.getId()).header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("无权")));
    }

    @Test
    void updateAddress_setsDefault() throws Exception {
        Address first = helper.createAddress(user.getId(), true);
        Address second = helper.createAddress(user.getId(), false);

        Map<String, Object> updateBody = Map.of("isDefault", 1);
        mockMvc.perform(put("/addresses/" + second.getId()).header("Authorization", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDefault").value(1));

        Address refreshedFirst = addressRepository.findById(first.getId()).orElseThrow();
        assertEquals(0, refreshedFirst.getIsDefault());
    }
}
