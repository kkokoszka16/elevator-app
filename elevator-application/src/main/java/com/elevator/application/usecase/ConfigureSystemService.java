package com.elevator.application.usecase;

import com.elevator.application.port.in.ConfigureSystemUseCase;
import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ConfigureSystemService implements ConfigureSystemUseCase {

    private final ElevatorRepository elevatorRepository;
    private final BuildingConfigRepository configRepository;
    private final ElevatorEventPublisher eventPublisher;

    @Override
    public void configure(BuildingConfig config) {
        configRepository.save(config);
        initializeElevators(config);
        eventPublisher.publishStateUpdate(elevatorRepository.findAll());
    }

    @Override
    public void reset() {
        var config = BuildingConfig.defaultConfig();
        configRepository.save(config);
        initializeElevators(config);
        eventPublisher.publishStateUpdate(elevatorRepository.findAll());
    }

    private void initializeElevators(BuildingConfig config) {
        elevatorRepository.deleteAll();

        List<Elevator> elevators = new ArrayList<>();
        var maxFloor = config.numberOfFloors() - 1;

        for (int i = 0; i < config.numberOfElevators(); i++) {
            elevators.add(new Elevator(new ElevatorId(i), maxFloor));
        }

        elevatorRepository.saveAll(elevators);
    }
}
