package com.elevator.application.port.in;

import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;

import java.util.List;
import java.util.Optional;

public interface GetSystemStatusUseCase {

    List<Elevator> getAllElevators();

    Optional<Elevator> getElevator(ElevatorId id);

    BuildingConfig getConfig();
}
