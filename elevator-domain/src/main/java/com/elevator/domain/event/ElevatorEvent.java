package com.elevator.domain.event;

import com.elevator.domain.model.ElevatorId;

import java.time.Instant;

public sealed interface ElevatorEvent permits
        ElevatorMovedEvent,
        ElevatorArrivedEvent,
        DoorStateChangedEvent,
        FloorRequestedEvent,
        ElevatorStateChangedEvent {

    ElevatorId elevatorId();

    Instant timestamp();

    String eventType();
}
