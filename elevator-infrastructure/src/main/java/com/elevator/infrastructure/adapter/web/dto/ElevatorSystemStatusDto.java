package com.elevator.infrastructure.adapter.web.dto;

import java.util.List;

public record ElevatorSystemStatusDto(
        int numberOfFloors,
        int numberOfElevators,
        List<ElevatorDto> elevators
) {}
