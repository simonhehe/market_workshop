package com.example.mall.common;

public class CartAccessDeniedException extends BusinessException {

    public CartAccessDeniedException(String message) {
        super("CART_ITEM_FORBIDDEN", message);
    }
}
