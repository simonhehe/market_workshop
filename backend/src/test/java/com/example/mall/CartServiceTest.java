package com.example.mall;

import com.example.mall.entity.Cart;
import com.example.mall.entity.Product;
import com.example.mall.repository.CartRepository;
import com.example.mall.repository.ProductRepository;
import com.example.mall.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @InjectMocks CartService cartService;

    @Test
    void addToCart_createsNewItem() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(5);
        product.setPointsRequired(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdAndProductId(2L, 1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cart cart = cartService.addToCart(2L, 1L, 2);
        assertEquals(2, cart.getQuantity());
    }

    @Test
    void updateQuantity_deletesWhenZero() {
        Cart cart = new Cart();
        cart.setId(9L);
        when(cartRepository.findById(9L)).thenReturn(Optional.of(cart));

        assertNull(cartService.updateQuantity(9L, 0));
        verify(cartRepository).delete(cart);
    }

    @Test
    void addToCart_mergesExisting() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Cart existing = new Cart();
        existing.setQuantity(1);
        when(cartRepository.findByUserIdAndProductId(2L, 1L)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cart cart = cartService.addToCart(2L, 1L, 2);
        assertEquals(3, cart.getQuantity());
    }

    @Test
    void addToCart_insufficientStock() {
        Product product = new Product();
        product.setId(1L);
        product.setStock(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addToCart(2L, 1L, 2));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    void updateQuantity_savesWhenPositive() {
        Cart cart = new Cart();
        cart.setId(9L);
        cart.setQuantity(1);
        when(cartRepository.findById(9L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cart updated = cartService.updateQuantity(9L, 5);
        assertEquals(5, updated.getQuantity());
    }

    @Test
    void selectCartItem_rejectsForeignUser() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(99L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.selectCartItem(1L, 2L, 1));
        assertTrue(ex.getMessage().contains("无权"));
    }

    @Test
    void selectCartItem_rejectsInvalidFlag() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(2L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.selectCartItem(1L, 2L, 2));
        assertTrue(ex.getMessage().contains("无效"));
    }

    @Test
    void selectCartItem_rejectsNullFlag() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(2L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.selectCartItem(1L, 2L, null));
        assertTrue(ex.getMessage().contains("无效"));
    }

    @Test
    void getCartSummary_countsSelectedOnly() {
        Cart selected = new Cart();
        selected.setIsSelected(1);
        selected.setProductId(1L);
        selected.setQuantity(2);
        Product product = new Product();
        product.setId(1L);
        product.setPointsRequired(30);
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(selected));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        var summary = cartService.getCartSummary(1L);
        assertEquals(60, summary.get("totalPoints"));
    }

    @Test
    void getCartSummary_skipsMissingProduct() {
        Cart selected = new Cart();
        selected.setIsSelected(1);
        selected.setProductId(99L);
        selected.setQuantity(1);
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(selected));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        var summary = cartService.getCartSummary(1L);
        assertEquals(0, summary.get("totalPoints"));
    }
}
