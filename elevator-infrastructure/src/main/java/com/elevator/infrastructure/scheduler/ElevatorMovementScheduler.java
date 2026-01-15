package com.elevator.infrastructure.scheduler;

import com.elevator.application.port.in.ElevatorSimulationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElevatorMovementScheduler {

    private final ElevatorSimulationUseCase simulationUseCase;

    @Scheduled(fixedRateString = "${elevator.system.tick-interval-ms:1000}")
    public void tick() {
        if (simulationUseCase.isRunning()) {
            log.trace("Simulation tick");
            simulationUseCase.tick();
        }
    }
}
