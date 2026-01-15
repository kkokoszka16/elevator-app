package com.elevator.domain.model;

public enum DoorState {
    OPEN,
    CLOSED,
    OPENING,
    CLOSING;

    public boolean canMove() {
        return this == CLOSED;
    }

    public boolean isFullyOpen() {
        return this == OPEN;
    }

    public boolean isFullyClosed() {
        return this == CLOSED;
    }
}
