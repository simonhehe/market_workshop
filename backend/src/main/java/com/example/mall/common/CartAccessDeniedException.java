package com.example.mall.common;

public class CartAccessDeniedException extends RuntimeException {

    public CartAccessDeniedException(String message) {
        super(message);
    }
}
