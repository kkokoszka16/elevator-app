package com.elevator.domain.event;

import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.domain.model.RequestSource;

import java.time.Instant;

public record FloorRequestedEvent(
        ElevatorId elevatorId,
        Floor floor,
        RequestSource source,
        Instant timestamp
) implements ElevatorEvent {

    public FloorRequestedEvent(ElevatorId elevatorId, Floor floor, RequestSource source) {
        this(elevatorId, floor, source, Instant.now());
    }

    @Override
    public String eventType() {
        return "FLOOR_REQUESTED";
    }
}
