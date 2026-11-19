package com.example.mall.common;

public class CartItemNotFoundException extends BusinessException {

    public CartItemNotFoundException(String message) {
        super("CART_404", message);
    }
}
