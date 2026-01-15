package com.elevator.domain.event;

import com.elevator.domain.model.Direction;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;

import java.time.Instant;

public record ElevatorMovedEvent(
        ElevatorId elevatorId,
        Floor fromFloor,
        Floor toFloor,
        Direction direction,
        Instant timestamp
) implements ElevatorEvent {

    public ElevatorMovedEvent(ElevatorId elevatorId, Floor fromFloor, Floor toFloor, Direction direction) {
        this(elevatorId, fromFloor, toFloor, direction, Instant.now());
    }

    @Override
    public String eventType() {
        return "ELEVATOR_MOVED";
    }
}
