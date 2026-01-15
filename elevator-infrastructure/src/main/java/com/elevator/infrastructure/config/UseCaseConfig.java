package com.elevator.infrastructure.config;

import com.elevator.application.port.in.CallElevatorUseCase;
import com.elevator.application.port.in.ConfigureSystemUseCase;
import com.elevator.application.port.in.ElevatorSimulationUseCase;
import com.elevator.application.port.in.GetSystemStatusUseCase;
import com.elevator.application.port.in.SelectFloorUseCase;
import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.application.usecase.CallElevatorService;
import com.elevator.application.usecase.ConfigureSystemService;
import com.elevator.application.usecase.ElevatorSimulationService;
import com.elevator.application.usecase.GetSystemStatusService;
import com.elevator.application.usecase.SelectFloorService;
import com.elevator.domain.service.ElevatorSchedulingStrategy;
import com.elevator.domain.service.LookSchedulingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ElevatorSchedulingStrategy elevatorSchedulingStrategy() {
        return new LookSchedulingStrategy();
    }

    @Bean
    public CallElevatorUseCase callElevatorUseCase(
            ElevatorRepository elevatorRepository,
            BuildingConfigRepository configRepository,
            ElevatorSchedulingStrategy schedulingStrategy,
            ElevatorEventPublisher eventPublisher) {
        return new CallElevatorService(elevatorRepository, configRepository, schedulingStrategy, eventPublisher);
    }

    @Bean
    public SelectFloorUseCase selectFloorUseCase(
            ElevatorRepository elevatorRepository,
            BuildingConfigRepository configRepository,
            ElevatorEventPublisher eventPublisher) {
        return new SelectFloorService(elevatorRepository, configRepository, eventPublisher);
    }

    @Bean
    public GetSystemStatusUseCase getSystemStatusUseCase(
            ElevatorRepository elevatorRepository,
            BuildingConfigRepository configRepository) {
        return new GetSystemStatusService(elevatorRepository, configRepository);
    }

    @Bean
    public ConfigureSystemUseCase configureSystemUseCase(
            ElevatorRepository elevatorRepository,
            BuildingConfigRepository configRepository,
            ElevatorEventPublisher eventPublisher) {
        return new ConfigureSystemService(elevatorRepository, configRepository, eventPublisher);
    }

    @Bean
    public ElevatorSimulationUseCase elevatorSimulationUseCase(
            ElevatorRepository elevatorRepository,
            BuildingConfigRepository configRepository,
            ElevatorEventPublisher eventPublisher) {
        return new ElevatorSimulationService(elevatorRepository, configRepository, eventPublisher);
    }
}
