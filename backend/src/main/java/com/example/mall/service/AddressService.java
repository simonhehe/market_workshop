package com.example.mall.service;

import com.example.mall.entity.Address;
import com.example.mall.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressService {
    
    private final AddressRepository addressRepository;
    
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdAndDeletedAtIsNullOrderByIsDefaultDesc(userId);
    }
    
    public Address createAddress(Long userId, Address address) {
        address.setUserId(userId);
        
        // 如果设置为默认地址，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            Optional<Address> defaultAddress = addressRepository.findByUserIdAndIsDefaultAndDeletedAtIsNull(userId, 1);
            defaultAddress.ifPresent(addr -> {
                addr.setIsDefault(0);
                addressRepository.save(addr);
            });
        }
        
        return addressRepository.save(address);
    }
    
    @Transactional
    public Address updateAddress(Long addressId, Long userId, Address address) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("收货地址不存在"));
        
        if (!existingAddress.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此地址");
        }
        
        if (address.getReceiver() != null) {
            existingAddress.setReceiver(address.getReceiver());
        }
        if (address.getPhone() != null) {
            existingAddress.setPhone(address.getPhone());
        }
        if (address.getProvince() != null) {
            existingAddress.setProvince(address.getProvince());
        }
        if (address.getCity() != null) {
            existingAddress.setCity(address.getCity());
        }
        if (address.getDistrict() != null) {
            existingAddress.setDistrict(address.getDistrict());
        }
        if (address.getDetailAddress() != null) {
            existingAddress.setDetailAddress(address.getDetailAddress());
        }
        if (address.getPostalCode() != null) {
            existingAddress.setPostalCode(address.getPostalCode());
        }
        if (address.getIsDefault() != null) {
            // 如果设置为默认地址，取消其他默认地址
            if (address.getIsDefault() == 1) {
                Optional<Address> defaultAddress = addressRepository.findByUserIdAndIsDefaultAndDeletedAtIsNull(userId, 1);
                defaultAddress.ifPresent(addr -> {
                    if (!addr.getId().equals(addressId)) {
                        addr.setIsDefault(0);
                        addressRepository.save(addr);
                    }
                });
            }
            existingAddress.setIsDefault(address.getIsDefault());
        }
        
        return addressRepository.save(existingAddress);
    }
    
    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("收货地址不存在"));
        
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此地址");
        }
        
        address.setDeletedAt(java.time.LocalDateTime.now());
        addressRepository.save(address);
    }
}
