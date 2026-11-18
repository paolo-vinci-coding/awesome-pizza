package com.awesomepizza.exception;

import com.awesomepizza.domain.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String orderCode, OrderStatus current, OrderStatus target) {
        super("Order " + orderCode + " is " + current + " and cannot move to " + target);
    }
}
