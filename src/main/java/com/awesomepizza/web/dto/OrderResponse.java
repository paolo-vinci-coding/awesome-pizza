package com.awesomepizza.web.dto;

import com.awesomepizza.domain.OrderStatus;
import com.awesomepizza.domain.PizzaOrder;

import java.util.List;

public record OrderResponse(
        String code,
        OrderStatus status,
        List<OrderItemResponse> items
) {

    public static OrderResponse from(PizzaOrder order) {
        return new OrderResponse(
                order.getCode(),
                order.getStatus(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
