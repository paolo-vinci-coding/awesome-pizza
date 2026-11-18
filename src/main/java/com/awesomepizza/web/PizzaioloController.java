package com.awesomepizza.web;

import com.awesomepizza.service.OrderService;
import com.awesomepizza.web.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pizzaiolo")
@Tag(name = "Pizzaiolo", description = "Order queue management")
public class PizzaioloController {

    private final OrderService orderService;

    public PizzaioloController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/queue")
    @Operation(summary = "Pending orders, oldest first")
    public List<OrderResponse> getQueue() {
        return orderService.getQueue();
    }

    @GetMapping("/current")
    @Operation(summary = "Order currently in preparation (204 if none)")
    public ResponseEntity<OrderResponse> getCurrentOrder() {
        return orderService.getCurrentOrder()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/next")
    @Operation(summary = "Take the next order (204 if the queue is empty, 409 if one is already in preparation)")
    public ResponseEntity<OrderResponse> takeNextOrder() {
        return orderService.takeNextOrder()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/orders/{code}/ready")
    @Operation(summary = "Mark the order in preparation as ready")
    public OrderResponse completeOrder(@PathVariable String code) {
        return orderService.completeOrder(code);
    }
}
