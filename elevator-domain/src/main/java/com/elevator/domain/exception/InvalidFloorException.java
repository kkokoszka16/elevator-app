package com.elevator.domain.exception;

public class InvalidFloorException extends DomainException {

    private final int floor;

    public InvalidFloorException(int floor) {
        super("Invalid floor number: " + floor);
        this.floor = floor;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public String getCode() {
        return "INVALID_FLOOR";
    }
}
