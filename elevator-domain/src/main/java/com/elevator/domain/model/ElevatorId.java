package com.elevator.domain.model;

public record ElevatorId(int value) {

    public ElevatorId {
        if (value < 0) {
            throw new IllegalArgumentException("Elevator ID must be non-negative");
        }
    }
}
