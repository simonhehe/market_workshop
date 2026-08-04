package com.example.mall;

import com.example.mall.entity.Address;
import com.example.mall.entity.User;
import com.example.mall.repository.AddressRepository;
import com.example.mall.service.AddressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock AddressRepository addressRepository;
    @InjectMocks AddressService addressService;

    @Test
    void updateAddress_updatesFields() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setReceiver("old");
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Address patch = new Address();
        patch.setReceiver("new");
        patch.setPhone("13900000000");
        patch.setProvince("P");
        patch.setCity("C");
        patch.setDistrict("D");
        patch.setDetailAddress("detail");
        patch.setPostalCode("100000");

        Address updated = addressService.updateAddress(1L, 2L, patch);
        assertEquals("new", updated.getReceiver());
        assertEquals("100000", updated.getPostalCode());
    }

    @Test
    void updateAddress_setDefault_clearsOther() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(2L);
        Address otherDefault = new Address();
        otherDefault.setId(9L);
        otherDefault.setIsDefault(1);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.findByUserIdAndIsDefaultAndDeletedAtIsNull(2L, 1))
                .thenReturn(Optional.of(otherDefault));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Address patch = new Address();
        patch.setIsDefault(1);
        addressService.updateAddress(1L, 2L, patch);

        assertEquals(0, otherDefault.getIsDefault());
        assertEquals(1, existing.getIsDefault());
    }

    @Test
    void updateAddress_setDefault_skipsSelf() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setIsDefault(0);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.findByUserIdAndIsDefaultAndDeletedAtIsNull(2L, 1))
                .thenReturn(Optional.of(existing));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Address patch = new Address();
        patch.setIsDefault(1);
        addressService.updateAddress(1L, 2L, patch);

        verify(addressRepository, times(1)).save(existing);
    }

    @Test
    void deleteAddress_rejectsForeignUser() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(2L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.deleteAddress(1L, 9L));
        assertTrue(ex.getMessage().contains("无权"));
    }

    @Test
    void createAddress_withoutDefault() {
        Address incoming = new Address();
        incoming.setIsDefault(0);
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        addressService.createAddress(2L, incoming);

        verify(addressRepository, never()).findByUserIdAndIsDefaultAndDeletedAtIsNull(anyLong(), anyInt());
    }

    @Test
    void deleteAddress_notFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> addressService.deleteAddress(1L, 2L));
    }

    @Test
    void updateAddress_rejectsForeignUser() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(2L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.updateAddress(1L, 9L, new Address()));
        assertTrue(ex.getMessage().contains("无权"));
    }

    @Test
    void deleteAddress_marksDeleted() {
        Address existing = new Address();
        existing.setId(1L);
        existing.setUserId(2L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        addressService.deleteAddress(1L, 2L);
        assertNotNull(existing.getDeletedAt());
    }

    @Test
    void createAddress_clearsPreviousDefault() {
        Address oldDefault = new Address();
        oldDefault.setId(10L);
        oldDefault.setIsDefault(1);
        when(addressRepository.findByUserIdAndIsDefaultAndDeletedAtIsNull(2L, 1))
                .thenReturn(Optional.of(oldDefault));
        when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Address incoming = new Address();
        incoming.setIsDefault(1);
        addressService.createAddress(2L, incoming);

        assertEquals(0, oldDefault.getIsDefault());
        verify(addressRepository, times(2)).save(any());
    }
}
