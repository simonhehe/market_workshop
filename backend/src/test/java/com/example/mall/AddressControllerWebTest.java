package com.example.mall;

import com.example.mall.controller.AddressController;
import com.example.mall.entity.Address;
import com.example.mall.service.AddressService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AddressControllerWebTest {

    @Mock AddressService addressService;
    @InjectMocks AddressController addressController;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();
    }

    @Test
    void getUserAddresses_success() throws Exception {
        when(addressService.getUserAddresses(1L)).thenReturn(List.of(new Address()));

        mockMvc.perform(get("/addresses").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUserAddresses_serviceError() throws Exception {
        when(addressService.getUserAddresses(1L)).thenThrow(new RuntimeException("list fail"));

        mockMvc.perform(get("/addresses").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SYS_001"));
    }

    @Test
    void createAddress_success() throws Exception {
        Address saved = new Address();
        saved.setId(1L);
        when(addressService.createAddress(eq(1L), any(Address.class))).thenReturn(saved);

        mockMvc.perform(post("/addresses").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiver\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void createAddress_serviceError() throws Exception {
        when(addressService.createAddress(anyLong(), any(Address.class)))
                .thenThrow(new RuntimeException("create fail"));

        mockMvc.perform(post("/addresses").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiver\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("create fail"));
    }

    @Test
    void updateAddress_success() throws Exception {
        Address updated = new Address();
        updated.setId(2L);
        when(addressService.updateAddress(eq(2L), eq(1L), any(Address.class))).thenReturn(updated);

        mockMvc.perform(put("/addresses/2").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiver\":\"new\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2));
    }

    @Test
    void updateAddress_serviceError() throws Exception {
        when(addressService.updateAddress(anyLong(), anyLong(), any(Address.class)))
                .thenThrow(new RuntimeException("update fail"));

        mockMvc.perform(put("/addresses/2").requestAttr("userId", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiver\":\"new\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SYS_001"));
    }

    @Test
    void deleteAddress_success() throws Exception {
        mockMvc.perform(delete("/addresses/2").requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteAddress_serviceError() throws Exception {
        doThrow(new RuntimeException("delete fail")).when(addressService).deleteAddress(2L, 1L);

        mockMvc.perform(delete("/addresses/2").requestAttr("userId", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("delete fail"));
    }
}
