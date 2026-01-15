package com.elevator.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "elevator.system")
public class ElevatorProperties {

    private int defaultFloors = 10;
    private int defaultElevators = 3;
    private int doorOpenDurationSeconds = 3;
    private int floorTravelDurationSeconds = 2;
    private long tickIntervalMs = 1000;
    private int requestsPerMinute = 100;
}
