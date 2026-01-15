package com.elevator.domain.event;

import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.ElevatorState;

import java.time.Instant;

public record ElevatorStateChangedEvent(
        ElevatorId elevatorId,
        ElevatorState previousState,
        ElevatorState newState,
        Instant timestamp
) implements ElevatorEvent {

    public ElevatorStateChangedEvent(ElevatorId elevatorId, ElevatorState previousState, ElevatorState newState) {
        this(elevatorId, previousState, newState, Instant.now());
    }

    @Override
    public String eventType() {
        return "ELEVATOR_STATE_CHANGED";
    }
}
