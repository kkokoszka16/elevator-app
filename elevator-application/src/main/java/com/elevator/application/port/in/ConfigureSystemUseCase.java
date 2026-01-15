package com.elevator.application.port.in;

import com.elevator.domain.model.BuildingConfig;

public interface ConfigureSystemUseCase {

    void configure(BuildingConfig config);

    void reset();
}
