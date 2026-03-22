package com.example.mall.service;

import com.example.mall.entity.Cart;
import com.example.mall.repository.CartRepository;
import com.example.mall.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void updateSelection_rejectsWrongUser() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(2L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.updateSelection(1L, 99L, 1));
        assertTrue(ex.getMessage().contains("无权操作此购物车项"));
    }

    @Test
    void updateSelection_rejectsInvalidFlag() {
        assertThrows(RuntimeException.class,
                () -> cartService.updateSelection(1L, 1L, 2));
    }

    @Test
    void removeFromCart_rejectsWrongUser() {
        Cart cart = new Cart();
        cart.setId(5L);
        cart.setUserId(3L);
        when(cartRepository.findById(5L)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.removeFromCart(5L, 1L));
        assertTrue(ex.getMessage().contains("无权操作此购物车项"));
        verify(cartRepository).findById(5L);
    }
}
