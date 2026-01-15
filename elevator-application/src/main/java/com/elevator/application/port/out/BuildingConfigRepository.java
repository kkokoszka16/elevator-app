package com.elevator.application.port.out;

import com.elevator.domain.model.BuildingConfig;

public interface BuildingConfigRepository {

    BuildingConfig get();

    void save(BuildingConfig config);
}
