package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.Address;
import com.example.mall.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Address>>> getUserAddresses(
            @RequestAttribute("userId") Long userId) {
        try {
            List<Address> addresses = addressService.getUserAddresses(userId);
            return ResponseEntity.ok(ApiResponse.success("获取收货地址列表成功", addresses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Address>> createAddress(
            @RequestAttribute("userId") Long userId,
            @RequestBody Address address) {
        try {
            Address savedAddress = addressService.createAddress(userId, address);
            return ResponseEntity.ok(ApiResponse.success("收货地址创建成功", savedAddress));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Address>> updateAddress(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestBody Address address) {
        try {
            Address updatedAddress = addressService.updateAddress(id, userId, address);
            return ResponseEntity.ok(ApiResponse.success("收货地址更新成功", updatedAddress));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        try {
            addressService.deleteAddress(id, userId);
            return ResponseEntity.ok(ApiResponse.success("收货地址删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SYS_001", e.getMessage()));
        }
    }
}
