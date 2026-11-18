package com.awesomepizza.web;

import com.awesomepizza.domain.OrderStatus;
import com.awesomepizza.exception.PizzaioloBusyException;
import com.awesomepizza.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static com.awesomepizza.web.OrderControllerTest.sampleOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PizzaioloController.class)
class PizzaioloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void queueListsPendingOrders() throws Exception {
        when(orderService.getQueue()).thenReturn(List.of(sampleOrder(OrderStatus.PENDING)));

        mockMvc.perform(get("/api/pizzaiolo/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("AP-ABCD2345"));
    }

    @Test
    void takeNextReturnsTakenOrder() throws Exception {
        when(orderService.takeNextOrder()).thenReturn(Optional.of(sampleOrder(OrderStatus.IN_PROGRESS)));

        mockMvc.perform(post("/api/pizzaiolo/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void takeNextWithEmptyQueueReturns204() throws Exception {
        when(orderService.takeNextOrder()).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/pizzaiolo/next"))
                .andExpect(status().isNoContent());
    }

    @Test
    void takeNextWhileBusyReturns409() throws Exception {
        when(orderService.takeNextOrder()).thenThrow(new PizzaioloBusyException("AP-ABCD2345"));

        mockMvc.perform(post("/api/pizzaiolo/next"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Operation not allowed"));
    }

    @Test
    void currentReturns204WhenNothingInProgress() throws Exception {
        when(orderService.getCurrentOrder()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pizzaiolo/current"))
                .andExpect(status().isNoContent());
    }

    @Test
    void markReady() throws Exception {
        when(orderService.completeOrder("AP-ABCD2345")).thenReturn(sampleOrder(OrderStatus.READY));

        mockMvc.perform(post("/api/pizzaiolo/orders/AP-ABCD2345/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
    }
}
