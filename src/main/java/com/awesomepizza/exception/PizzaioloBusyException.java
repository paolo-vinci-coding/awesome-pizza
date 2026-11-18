package com.awesomepizza.exception;

public class PizzaioloBusyException extends RuntimeException {

    public PizzaioloBusyException(String currentOrderCode) {
        super("An order is already in preparation (" + currentOrderCode
                + "): complete it before taking another one");
    }
}
