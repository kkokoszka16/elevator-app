package com.elevator;

import com.elevator.application.port.in.ConfigureSystemUseCase;
import com.elevator.application.port.in.ElevatorSimulationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class ElevatorSystemApplication implements CommandLineRunner {

    private final ConfigureSystemUseCase configureSystemUseCase;
    private final ElevatorSimulationUseCase simulationUseCase;

    public static void main(String[] args) {
        SpringApplication.run(ElevatorSystemApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("Initializing elevator system...");
        configureSystemUseCase.reset();
        simulationUseCase.start();
        log.info("Elevator system started with simulation running");
    }
}
