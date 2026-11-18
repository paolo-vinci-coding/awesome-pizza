package com.awesomepizza.web;

import com.awesomepizza.domain.OrderStatus;
import com.awesomepizza.exception.OrderNotFoundException;
import com.awesomepizza.exception.PizzaNotFoundException;
import com.awesomepizza.service.OrderService;
import com.awesomepizza.web.dto.CreateOrderRequest;
import com.awesomepizza.web.dto.OrderItemResponse;
import com.awesomepizza.web.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrderReturns201WithLocationAndCode() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(sampleOrder(OrderStatus.PENDING));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"pizzaId": 1, "quantity": 2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/orders/AP-ABCD2345"))
                .andExpect(jsonPath("$.code").value("AP-ABCD2345"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrderValidatesInput() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"pizzaId": 1, "quantity": 0}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors['items[0].quantity']").exists());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    void createOrderRejectsEmptyItems() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void createOrderWithUnknownPizzaReturns400() throws Exception {
        when(orderService.createOrder(any())).thenThrow(new PizzaNotFoundException(99L));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"pizzaId": 99, "quantity": 1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Pizza not on the menu: 99"));
    }

    @Test
    void getOrderReturnsStatus() throws Exception {
        when(orderService.getOrder("AP-ABCD2345")).thenReturn(sampleOrder(OrderStatus.IN_PROGRESS));

        mockMvc.perform(get("/api/orders/AP-ABCD2345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.items[0].pizzaName").value("Margherita"));
    }

    @Test
    void getUnknownOrderReturns404() throws Exception {
        when(orderService.getOrder("NOPE")).thenThrow(new OrderNotFoundException("NOPE"));

        mockMvc.perform(get("/api/orders/NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Order not found"))
                .andExpect(jsonPath("$.detail").value("Order not found: NOPE"));
    }

    static OrderResponse sampleOrder(OrderStatus status) {
        return new OrderResponse("AP-ABCD2345", status,
                List.of(new OrderItemResponse("Margherita", 2)));
    }
}
