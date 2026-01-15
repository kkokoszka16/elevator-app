package com.elevator.domain.event;

import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;

import java.time.Instant;

public record ElevatorArrivedEvent(
        ElevatorId elevatorId,
        Floor floor,
        Instant timestamp
) implements ElevatorEvent {

    public ElevatorArrivedEvent(ElevatorId elevatorId, Floor floor) {
        this(elevatorId, floor, Instant.now());
    }

    @Override
    public String eventType() {
        return "ELEVATOR_ARRIVED";
    }
}
