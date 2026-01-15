package com.elevator.application.port.in;

public interface ElevatorSimulationUseCase {

    void tick();

    void start();

    void stop();

    boolean isRunning();
}
