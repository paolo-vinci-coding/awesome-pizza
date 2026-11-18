package com.awesomepizza.service;

import com.awesomepizza.domain.OrderStatus;
import com.awesomepizza.domain.Pizza;
import com.awesomepizza.domain.PizzaOrder;
import com.awesomepizza.exception.InvalidOrderStateException;
import com.awesomepizza.exception.OrderNotFoundException;
import com.awesomepizza.exception.PizzaNotFoundException;
import com.awesomepizza.exception.PizzaioloBusyException;
import com.awesomepizza.repository.OrderRepository;
import com.awesomepizza.repository.PizzaRepository;
import com.awesomepizza.util.OrderCodeGenerator;
import com.awesomepizza.web.dto.CreateOrderRequest;
import com.awesomepizza.web.dto.OrderItemRequest;
import com.awesomepizza.web.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PizzaRepository pizzaRepository;
    @Mock
    private OrderCodeGenerator codeGenerator;

    private OrderService service;
    private Pizza margherita;

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, pizzaRepository, codeGenerator);

        margherita = new Pizza("Margherita", null);

        ReflectionTestUtils.setField(margherita, "id", 1L);
    }

    // ---------------------------------------------------------------- customer

    @Test
    void createOrderSavesPendingOrderWithGeneratedCode() {
        when(codeGenerator.generate()).thenReturn("AP-AAAA2222");
        when(orderRepository.existsByCode("AP-AAAA2222")).thenReturn(false);
        when(pizzaRepository.findAllById(List.of(1L))).thenReturn(List.of(margherita));
        when(orderRepository.save(any(PizzaOrder.class))).thenAnswer(call -> call.getArgument(0));

        OrderResponse response = service.createOrder(
                new CreateOrderRequest(List.of(new OrderItemRequest(1L, 2))));

        assertThat(response.code()).isEqualTo("AP-AAAA2222");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.items().get(0).pizzaName()).isEqualTo("Margherita");
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void createOrderGeneratesAnotherCodeIfAlreadyPresent() {
        when(codeGenerator.generate()).thenReturn("AP-TAKEN000", "AP-FREE0000");
        when(orderRepository.existsByCode("AP-TAKEN000")).thenReturn(true);
        when(orderRepository.existsByCode("AP-FREE0000")).thenReturn(false);
        when(pizzaRepository.findAllById(List.of(1L))).thenReturn(List.of(margherita));
        when(orderRepository.save(any(PizzaOrder.class))).thenAnswer(call -> call.getArgument(0));

        OrderResponse response = service.createOrder(
                new CreateOrderRequest(List.of(new OrderItemRequest(1L, 1))));

        assertThat(response.code()).isEqualTo("AP-FREE0000");
    }

    @Test
    void createOrderFailsWhenPizzaIsNotInMenu() {
        when(pizzaRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.createOrder(
                new CreateOrderRequest(List.of(new OrderItemRequest(99L, 1)))))
                .isInstanceOf(PizzaNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderReturnsItsStatus() {
        when(orderRepository.findByCode("AP-1")).thenReturn(Optional.of(newOrder("AP-1")));

        assertThat(service.getOrder("AP-1").status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void getOrderFailsForUnknownCode() {
        when(orderRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrder("NOPE"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // -------------------------------------------------------------- pizzaiolo

    @Test
    void queueReturnsPendingOrders() {
        when(orderRepository.findByStatusOrderByIdAsc(OrderStatus.PENDING))
                .thenReturn(List.of(newOrder("AP-1"), newOrder("AP-2")));

        List<OrderResponse> queue = service.getQueue();

        assertThat(queue).hasSize(2);
        assertThat(queue.get(0).code()).isEqualTo("AP-1");
        assertThat(queue.get(1).code()).isEqualTo("AP-2");
    }

    @Test
    void takeNextOrderPutsFirstPendingOrderInProgress() {
        PizzaOrder first = newOrder("AP-1");
        when(orderRepository.findFirstByStatusOrderByIdAsc(OrderStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        when(orderRepository.findFirstByStatusOrderByIdAsc(OrderStatus.PENDING)).thenReturn(Optional.of(first));

        Optional<OrderResponse> taken = service.takeNextOrder();

        assertThat(taken).isPresent();
        assertThat(taken.get().code()).isEqualTo("AP-1");
        assertThat(first.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
    }

    @Test
    void takeNextOrderReturnsEmptyWhenQueueIsEmpty() {
        when(orderRepository.findFirstByStatusOrderByIdAsc(OrderStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        when(orderRepository.findFirstByStatusOrderByIdAsc(OrderStatus.PENDING)).thenReturn(Optional.empty());

        assertThat(service.takeNextOrder()).isEmpty();
    }

    @Test
    void takeNextOrderFailsWhileAnotherOrderIsInProgress() {
        PizzaOrder current = newOrder("AP-1");
        current.markInProgress();
        when(orderRepository.findFirstByStatusOrderByIdAsc(OrderStatus.IN_PROGRESS)).thenReturn(Optional.of(current));

        assertThatThrownBy(() -> service.takeNextOrder())
                .isInstanceOf(PizzaioloBusyException.class);
    }

    @Test
    void getCurrentOrderIsEmptyWhenNothingIsInProgress() {
        when(orderRepository.findFirstByStatusOrderByIdAsc(OrderStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        assertThat(service.getCurrentOrder()).isEmpty();
    }

    @Test
    void completeOrderMarksItReady() {
        PizzaOrder order = newOrder("AP-1");
        order.markInProgress();
        when(orderRepository.findByCode("AP-1")).thenReturn(Optional.of(order));

        assertThat(service.completeOrder("AP-1").status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void completeOrderFailsIfOrderWasNotTaken() {
        when(orderRepository.findByCode("AP-1")).thenReturn(Optional.of(newOrder("AP-1")));

        assertThatThrownBy(() -> service.completeOrder("AP-1"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    private PizzaOrder newOrder(String code) {
        PizzaOrder order = new PizzaOrder(code);
        order.addItem(margherita, 1);
        return order;
    }
}
