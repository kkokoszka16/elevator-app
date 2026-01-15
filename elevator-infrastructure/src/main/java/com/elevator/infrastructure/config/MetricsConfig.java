package com.elevator.infrastructure.config;

import com.elevator.application.port.in.GetSystemStatusUseCase;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    @Bean
    public MeterBinder elevatorMetrics(GetSystemStatusUseCase getSystemStatusUseCase) {
        return registry -> {
            Gauge.builder("elevator.system.floors", getSystemStatusUseCase,
                            status -> status.getConfig().numberOfFloors())
                    .description("Number of floors in the building")
                    .tag("application", "elevator-system")
                    .register(registry);

            Gauge.builder("elevator.system.elevators.total", getSystemStatusUseCase,
                            status -> status.getConfig().numberOfElevators())
                    .description("Total number of elevators")
                    .tag("application", "elevator-system")
                    .register(registry);

            Gauge.builder("elevator.system.elevators.active", getSystemStatusUseCase,
                            status -> status.getAllElevators().size())
                    .description("Number of active elevators")
                    .tag("application", "elevator-system")
                    .register(registry);

            Gauge.builder("elevator.system.requests.pending", getSystemStatusUseCase,
                            status -> status.getAllElevators().stream()
                                    .mapToInt(e -> e.getDestinations().size())
                                    .sum())
                    .description("Total pending floor requests")
                    .tag("application", "elevator-system")
                    .register(registry);
        };
    }
}
