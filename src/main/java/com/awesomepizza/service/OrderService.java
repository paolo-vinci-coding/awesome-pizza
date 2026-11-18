package com.awesomepizza.service;

import com.awesomepizza.domain.OrderStatus;
import com.awesomepizza.domain.Pizza;
import com.awesomepizza.domain.PizzaOrder;
import com.awesomepizza.exception.OrderNotFoundException;
import com.awesomepizza.exception.PizzaNotFoundException;
import com.awesomepizza.exception.PizzaioloBusyException;
import com.awesomepizza.repository.OrderRepository;
import com.awesomepizza.repository.PizzaRepository;
import com.awesomepizza.util.OrderCodeGenerator;
import com.awesomepizza.web.dto.CreateOrderRequest;
import com.awesomepizza.web.dto.OrderItemRequest;
import com.awesomepizza.web.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PizzaRepository pizzaRepository;
    private final OrderCodeGenerator codeGenerator;

    public OrderService(OrderRepository orderRepository,
                        PizzaRepository pizzaRepository,
                        OrderCodeGenerator codeGenerator) {
        this.orderRepository = orderRepository;
        this.pizzaRepository = pizzaRepository;
        this.codeGenerator = codeGenerator;
    }

    // ---------------------------------------------------------------- customer

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Map<Long, Pizza> pizzasById = loadPizzas(request.items());

        PizzaOrder order = new PizzaOrder(generateUniqueCode());
        for (OrderItemRequest item : request.items()) {
            order.addItem(pizzasById.get(item.pizzaId()), item.quantity());
        }
        return OrderResponse.from(orderRepository.save(order));
    }

    /** Returns the order with the given code, so the customer can track its status. */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String code) {
        return OrderResponse.from(findByCode(code));
    }

    // -------------------------------------------------------------- pizzaiolo

    @Transactional(readOnly = true)
    public List<OrderResponse> getQueue() {
        return orderRepository.findByStatusOrderByIdAsc(OrderStatus.PENDING).stream()
                .map(OrderResponse::from)
                .toList();
    }

    /** Returns the order in preparation, if any. */
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getCurrentOrder() {
        return findOrderInProgress().map(OrderResponse::from);
    }

    @Transactional
    public Optional<OrderResponse> takeNextOrder() {
        findOrderInProgress().ifPresent(current -> {
            throw new PizzaioloBusyException(current.getCode());
        });

        return orderRepository.findFirstByStatusOrderByIdAsc(OrderStatus.PENDING)
                .map(order -> {
                    order.markInProgress();
                    return OrderResponse.from(order);
                });
    }

    @Transactional
    public OrderResponse completeOrder(String code) {
        PizzaOrder order = findByCode(code);
        order.markReady();
        return OrderResponse.from(order);
    }

    // ----------------------------------------------------------------

    private Map<Long, Pizza> loadPizzas(List<OrderItemRequest> items) {
        List<Long> ids = items.stream().map(OrderItemRequest::pizzaId).distinct().toList();
        Map<Long, Pizza> found = pizzaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Pizza::getId, Function.identity()));
        // check presence from customer list
        ids.stream()
                .filter(id -> !found.containsKey(id))
                .findFirst()
                .ifPresent(missing -> {
                    throw new PizzaNotFoundException(missing);
                });
        return found;
    }

    private Optional<PizzaOrder> findOrderInProgress() {
        return orderRepository.findFirstByStatusOrderByIdAsc(OrderStatus.IN_PROGRESS);
    }

    private PizzaOrder findByCode(String code) {
        return orderRepository.findByCode(code).orElseThrow(() -> new OrderNotFoundException(code));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = codeGenerator.generate();
        } while (orderRepository.existsByCode(code));
        return code;
    }
}
