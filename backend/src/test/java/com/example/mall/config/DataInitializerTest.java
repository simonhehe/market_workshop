package com.example.mall.config;

import com.example.mall.entity.Address;
import com.example.mall.entity.User;
import com.example.mall.repository.AddressRepository;
import com.example.mall.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Test
    void seedsTwoMockAddressesForUser1Idempotently() {
        User user1 = userRepository.findByUsername("user1").orElseThrow();
        List<Address> before = addressRepository
                .findByUserIdAndDeletedAtIsNullOrderByIsDefaultDesc(user1.getId());

        assertThat(before).hasSize(2);
        assertThat(before).allSatisfy(address -> {
            assertThat(address.getUserId()).isEqualTo(user1.getId());
            assertThat(address.getReceiver()).isNotBlank();
            assertThat(address.getPhone()).isNotBlank();
            assertThat(address.getDetailAddress()).isNotBlank();
        });

        dataInitializer.run();

        List<Address> after = addressRepository
                .findByUserIdAndDeletedAtIsNullOrderByIsDefaultDesc(user1.getId());
        assertThat(after).hasSize(2);
        assertThat(after).extracting(Address::getId)
                .containsExactlyInAnyOrderElementsOf(before.stream().map(Address::getId).toList());
    }
}
