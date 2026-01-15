package com.elevator.domain.service;

import com.elevator.domain.model.Direction;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;

import java.util.List;
import java.util.Optional;

public interface ElevatorSchedulingStrategy {

    Optional<ElevatorId> selectElevator(Floor requestFloor, Direction direction, List<Elevator> elevators);
}
