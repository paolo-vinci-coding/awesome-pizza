package com.awesomepizza.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String code) {
        super("Order not found: " + code);
    }
}
