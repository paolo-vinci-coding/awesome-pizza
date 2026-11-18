package com.awesomepizza.domain;

import com.awesomepizza.exception.InvalidOrderStateException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")
public class PizzaOrder {

    /** Defines the order FIFO of the queue */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Version
    private Long version;

    protected PizzaOrder() {
    }

    public PizzaOrder(String code) {
        this.code = code;
        this.status = OrderStatus.PENDING;
    }

    public void addItem(Pizza pizza, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        items.add(new OrderItem(this, pizza, quantity));
    }

    public void markInProgress() {
        changeStatus(OrderStatus.IN_PROGRESS);
    }

    public void markReady() {
        changeStatus(OrderStatus.READY);
    }

    private void changeStatus(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidOrderStateException(code, status, target);
        }
        this.status = target;
    }

    public String getCode() {
        return code;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
