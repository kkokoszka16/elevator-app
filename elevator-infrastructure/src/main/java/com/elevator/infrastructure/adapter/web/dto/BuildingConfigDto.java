package com.elevator.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BuildingConfigDto(
        @NotNull(message = "Number of floors is required")
        @Min(value = 1, message = "Number of floors must be at least 1")
        Integer numberOfFloors,

        @NotNull(message = "Number of elevators is required")
        @Min(value = 1, message = "Number of elevators must be at least 1")
        Integer numberOfElevators,

        @Min(value = 1, message = "Door open duration must be at least 1 second")
        Integer doorOpenDurationSeconds,

        @Min(value = 1, message = "Floor travel duration must be at least 1 second")
        Integer floorTravelDurationSeconds
) {}
