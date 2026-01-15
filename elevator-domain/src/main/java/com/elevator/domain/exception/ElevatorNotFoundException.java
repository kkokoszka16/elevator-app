package com.elevator.domain.exception;

import com.elevator.domain.model.ElevatorId;

public class ElevatorNotFoundException extends DomainException {

    private final ElevatorId elevatorId;

    public ElevatorNotFoundException(ElevatorId elevatorId) {
        super("Elevator not found: " + elevatorId.value());
        this.elevatorId = elevatorId;
    }

    public ElevatorId getElevatorId() {
        return elevatorId;
    }

    @Override
    public String getCode() {
        return "ELEVATOR_NOT_FOUND";
    }
}
