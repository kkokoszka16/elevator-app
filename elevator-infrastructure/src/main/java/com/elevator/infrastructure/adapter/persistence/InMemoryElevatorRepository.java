package com.elevator.infrastructure.adapter.persistence;

import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryElevatorRepository implements ElevatorRepository {

    private final Map<Integer, Elevator> elevators = new ConcurrentHashMap<>();

    @Override
    public List<Elevator> findAll() {
        return new ArrayList<>(elevators.values());
    }

    @Override
    public Optional<Elevator> findById(ElevatorId id) {
        return Optional.ofNullable(elevators.get(id.value()));
    }

    @Override
    public void save(Elevator elevator) {
        elevators.put(elevator.getId().value(), elevator);
    }

    @Override
    public void saveAll(List<Elevator> elevatorList) {
        elevatorList.forEach(e -> elevators.put(e.getId().value(), e));
    }

    @Override
    public void deleteAll() {
        elevators.clear();
    }
}
