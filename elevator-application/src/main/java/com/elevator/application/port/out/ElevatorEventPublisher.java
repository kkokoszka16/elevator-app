package com.elevator.application.port.out;

import com.elevator.domain.event.ElevatorEvent;
import com.elevator.domain.model.Elevator;

import java.util.List;

public interface ElevatorEventPublisher {

    void publish(ElevatorEvent event);

    void publishStateUpdate(List<Elevator> elevators);
}
