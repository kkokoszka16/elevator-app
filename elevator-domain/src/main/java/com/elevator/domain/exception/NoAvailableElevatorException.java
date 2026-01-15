package com.elevator.domain.exception;

import com.elevator.domain.model.Floor;

public class NoAvailableElevatorException extends DomainException {

    private final Floor requestedFloor;

    public NoAvailableElevatorException(Floor requestedFloor) {
        super("No available elevator for floor: " + requestedFloor.number());
        this.requestedFloor = requestedFloor;
    }

    public Floor getRequestedFloor() {
        return requestedFloor;
    }

    @Override
    public String getCode() {
        return "NO_AVAILABLE_ELEVATOR";
    }
}
