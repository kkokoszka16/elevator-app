package com.elevator.infrastructure.adapter.web.dto;

import java.util.List;

public record ElevatorDto(
        int id,
        int currentFloor,
        String direction,
        String doorState,
        String state,
        List<Integer> destinations
) {}
