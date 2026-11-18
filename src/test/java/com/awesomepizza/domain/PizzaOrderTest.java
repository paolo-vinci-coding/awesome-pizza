package com.awesomepizza.domain;

import com.awesomepizza.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PizzaOrderTest {

    private final Pizza margherita = new Pizza("Margherita", "Tomato, mozzarella");

    @Test
    void newOrderIsPending() {
        PizzaOrder order = new PizzaOrder("AP-TEST0001");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void addItemKeepsPizzaAndQuantity() {
        PizzaOrder order = new PizzaOrder("AP-TEST0001");
        order.addItem(margherita, 2);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getPizza()).isEqualTo(margherita);
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void addItemRejectsZeroQuantity() {
        PizzaOrder order = new PizzaOrder("AP-TEST0001");

        assertThatThrownBy(() -> order.addItem(margherita, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void orderGoesFromPendingToInProgressToReady() {
        PizzaOrder order = new PizzaOrder("AP-TEST0001");

        order.markInProgress();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

        order.markReady();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void cannotMarkReadyAnOrderNotInProgress() {
        PizzaOrder order = new PizzaOrder("AP-TEST0001");

        assertThatThrownBy(order::markReady)
                .isInstanceOf(InvalidOrderStateException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void cannotTakeAnOrderTwice() {
        PizzaOrder order = new PizzaOrder("AP-TEST0001");
        order.markInProgress();

        assertThatThrownBy(order::markInProgress)
                .isInstanceOf(InvalidOrderStateException.class);
    }
}
