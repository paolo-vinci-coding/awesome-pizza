package com.awesomepizza.exception;

public class PizzaNotFoundException extends RuntimeException {

    public PizzaNotFoundException(Long pizzaId) {
        super("Pizza not on the menu: " + pizzaId);
    }
}
