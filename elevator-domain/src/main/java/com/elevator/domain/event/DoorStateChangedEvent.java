package com.elevator.domain.event;

import com.elevator.domain.model.DoorState;
import com.elevator.domain.model.ElevatorId;

import java.time.Instant;

public record DoorStateChangedEvent(
        ElevatorId elevatorId,
        DoorState previousState,
        DoorState newState,
        Instant timestamp
) implements ElevatorEvent {

    public DoorStateChangedEvent(ElevatorId elevatorId, DoorState previousState, DoorState newState) {
        this(elevatorId, previousState, newState, Instant.now());
    }

    @Override
    public String eventType() {
        return "DOOR_STATE_CHANGED";
    }
}
