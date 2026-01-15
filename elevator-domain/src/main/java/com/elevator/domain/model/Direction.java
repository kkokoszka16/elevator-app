package com.elevator.domain.model;

public enum Direction {
    UP,
    DOWN,
    IDLE;

    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case IDLE -> IDLE;
        };
    }

    public boolean isMoving() {
        return this != IDLE;
    }
}
