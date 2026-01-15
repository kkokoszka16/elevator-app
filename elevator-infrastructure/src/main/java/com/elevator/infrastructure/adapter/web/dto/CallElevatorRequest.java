package com.elevator.infrastructure.adapter.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CallElevatorRequest(
        @NotNull(message = "Floor is required")
        @Min(value = 0, message = "Floor must be non-negative")
        Integer floor,

        @NotNull(message = "Direction is required")
        String direction
) {}
