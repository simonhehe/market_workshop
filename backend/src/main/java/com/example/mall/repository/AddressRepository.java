package com.example.mall.repository;

import com.example.mall.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserIdAndDeletedAtIsNullOrderByIsDefaultDesc(Long userId);
    Optional<Address> findByUserIdAndIsDefaultAndDeletedAtIsNull(Long userId, Integer isDefault);
}
