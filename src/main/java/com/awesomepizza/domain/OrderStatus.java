package com.awesomepizza.domain;

public enum OrderStatus {

    PENDING,
    IN_PROGRESS,
    READY;

    /** Allowed transitions: PENDING -> IN_PROGRESS -> READY. */
    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case PENDING -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == READY;
            case READY -> false;
        };
    }
}