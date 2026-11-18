package com.awesomepizza.web.dto;

import com.awesomepizza.domain.OrderItem;

public record OrderItemResponse(String pizzaName, int quantity) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getPizza().getName(), item.getQuantity());
    }
}
