package com.elevator.application.usecase;

import com.elevator.application.port.in.GetSystemStatusUseCase;
import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GetSystemStatusService implements GetSystemStatusUseCase {

    private final ElevatorRepository elevatorRepository;
    private final BuildingConfigRepository configRepository;

    @Override
    public List<Elevator> getAllElevators() {
        return elevatorRepository.findAll();
    }

    @Override
    public Optional<Elevator> getElevator(ElevatorId id) {
        return elevatorRepository.findById(id);
    }

    @Override
    public BuildingConfig getConfig() {
        return configRepository.get();
    }
}
