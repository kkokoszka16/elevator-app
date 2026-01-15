package com.elevator.application.port.out;

import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;

import java.util.List;
import java.util.Optional;

public interface ElevatorRepository {

    List<Elevator> findAll();

    Optional<Elevator> findById(ElevatorId id);

    void save(Elevator elevator);

    void saveAll(List<Elevator> elevators);

    void deleteAll();
}
