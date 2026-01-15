package com.elevator.infrastructure.adapter.persistence;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.domain.model.BuildingConfig;
import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicReference;

@Repository
public class InMemoryBuildingConfigRepository implements BuildingConfigRepository {

    private final AtomicReference<BuildingConfig> config = new AtomicReference<>(BuildingConfig.defaultConfig());

    @Override
    public BuildingConfig get() {
        return config.get();
    }

    @Override
    public void save(BuildingConfig newConfig) {
        config.set(newConfig);
    }
}
