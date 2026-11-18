package com.awesomepizza.web.dto;

import com.awesomepizza.domain.Pizza;

public record PizzaResponse(Long id, String name, String description) {

    public static PizzaResponse from(Pizza pizza) {
        return new PizzaResponse(pizza.getId(), pizza.getName(), pizza.getDescription());
    }
}
