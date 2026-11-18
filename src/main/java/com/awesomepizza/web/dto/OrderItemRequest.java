package com.awesomepizza.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long pizzaId,
        @NotNull @Min(1) @Max(20) Integer quantity
) {
}
