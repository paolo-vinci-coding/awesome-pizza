package com.awesomepizza.web;

import com.awesomepizza.service.OrderService;
import com.awesomepizza.web.dto.CreateOrderRequest;
import com.awesomepizza.web.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Customer", description = "Place and track an order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place an order and get the code to track it")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(created.code())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get the status of an order by its code")
    public OrderResponse getOrder(@PathVariable String code) {
        return orderService.getOrder(code);
    }
}
