package com.elevator.domain.model;

import java.time.Duration;

public record BuildingConfig(
        int numberOfFloors,
        int numberOfElevators,
        Duration doorOpenDuration,
        Duration floorTravelDuration
) {
    public BuildingConfig {
        if (numberOfFloors < 1) {
            throw new IllegalArgumentException("Number of floors must be at least 1");
        }
        if (numberOfElevators < 1) {
            throw new IllegalArgumentException("Number of elevators must be at least 1");
        }
        if (doorOpenDuration == null || doorOpenDuration.isNegative() || doorOpenDuration.isZero()) {
            throw new IllegalArgumentException("Door open duration must be positive");
        }
        if (floorTravelDuration == null || floorTravelDuration.isNegative() || floorTravelDuration.isZero()) {
            throw new IllegalArgumentException("Floor travel duration must be positive");
        }
    }

    public static BuildingConfig defaultConfig() {
        return new BuildingConfig(10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2));
    }

    public Floor topFloor() {
        return new Floor(numberOfFloors - 1);
    }

    public Floor groundFloor() {
        return new Floor(0);
    }

    public boolean isValidFloor(Floor floor) {
        return floor.number() >= 0 && floor.number() < numberOfFloors;
    }
}
