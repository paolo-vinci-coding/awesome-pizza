package com.awesomepizza.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest(name = "{0} -> {1} : {2}")
    @CsvSource({
            "PENDING,     IN_PROGRESS, true",
            "PENDING,     READY,       false",
            "PENDING,     PENDING,     false",
            "IN_PROGRESS, READY,       true",
            "IN_PROGRESS, PENDING,     false",
            "IN_PROGRESS, IN_PROGRESS, false",
            "READY,       PENDING,     false",
            "READY,       IN_PROGRESS, false",
            "READY,       READY,       false",
    })
    void transitions(OrderStatus from, OrderStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }
}
