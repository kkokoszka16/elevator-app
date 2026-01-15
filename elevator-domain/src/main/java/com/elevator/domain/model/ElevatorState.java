package com.elevator.domain.model;

public enum ElevatorState {
    IDLE,
    MOVING,
    STOPPED,
    DOOR_OPENING,
    DOOR_OPEN,
    DOOR_CLOSING;

    public boolean canAcceptNewDestination() {
        return true;
    }

    public boolean isIdle() {
        return this == IDLE;
    }

    public boolean isMoving() {
        return this == MOVING;
    }
}
