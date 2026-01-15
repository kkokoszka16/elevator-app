package com.elevator.infrastructure.config;

import com.elevator.application.port.in.ElevatorSimulationUseCase;
import com.elevator.application.port.in.GetSystemStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElevatorSystemHealthIndicator implements HealthIndicator {

    private final GetSystemStatusUseCase getSystemStatusUseCase;
    private final ElevatorSimulationUseCase simulationUseCase;

    @Override
    public Health health() {
        try {
            var config = getSystemStatusUseCase.getConfig();
            var elevators = getSystemStatusUseCase.getAllElevators();

            return Health.up()
                    .withDetail("numberOfFloors", config.numberOfFloors())
                    .withDetail("numberOfElevators", config.numberOfElevators())
                    .withDetail("activeElevators", elevators.size())
                    .withDetail("simulationRunning", simulationUseCase.isRunning())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
