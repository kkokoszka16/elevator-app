package com.elevator.domain.service;

import com.elevator.domain.model.Direction;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LookSchedulingStrategy implements ElevatorSchedulingStrategy {

    @Override
    public Optional<ElevatorId> selectElevator(Floor requestFloor, Direction direction, List<Elevator> elevators) {
        if (elevators.isEmpty()) {
            return Optional.empty();
        }

        return findBestElevator(requestFloor, direction, elevators);
    }

    private Optional<ElevatorId> findBestElevator(Floor requestFloor, Direction direction, List<Elevator> elevators) {
        var onTheWayElevator = findElevatorOnTheWay(requestFloor, direction, elevators);
        if (onTheWayElevator.isPresent()) {
            return onTheWayElevator;
        }

        var idleElevator = findClosestIdleElevator(requestFloor, elevators);
        if (idleElevator.isPresent()) {
            return idleElevator;
        }

        return findLeastBusyElevator(elevators);
    }

    private Optional<ElevatorId> findClosestIdleElevator(Floor requestFloor, List<Elevator> elevators) {
        return elevators.stream()
                .filter(Elevator::isIdle)
                .min(Comparator.comparingInt(e -> e.distanceTo(requestFloor)))
                .map(Elevator::getId);
    }

    private Optional<ElevatorId> findElevatorOnTheWay(Floor requestFloor, Direction direction, List<Elevator> elevators) {
        return elevators.stream()
                .filter(e -> !e.isIdle())
                .filter(e -> e.canServiceRequest(requestFloor, direction))
                .filter(e -> e.getDirection() == direction)
                .min(Comparator.comparingInt(e -> calculateEffectiveDistance(e, requestFloor, direction)))
                .map(Elevator::getId);
    }

    private Optional<ElevatorId> findLeastBusyElevator(List<Elevator> elevators) {
        return elevators.stream()
                .min(Comparator.comparingInt(e -> e.getDestinations().size()))
                .map(Elevator::getId);
    }

    private int calculateEffectiveDistance(Elevator elevator, Floor requestFloor, Direction direction) {
        var currentFloor = elevator.getCurrentFloor();
        var elevatorDirection = elevator.getDirection();

        if (elevatorDirection == Direction.IDLE) {
            return currentFloor.distanceTo(requestFloor);
        }

        if (elevatorDirection == direction) {
            if (isInSameDirection(currentFloor, requestFloor, direction)) {
                return currentFloor.distanceTo(requestFloor);
            }
        }

        return currentFloor.distanceTo(requestFloor) + 1000;
    }

    private boolean isInSameDirection(Floor currentFloor, Floor requestFloor, Direction direction) {
        if (direction == Direction.UP) {
            return requestFloor.number() >= currentFloor.number();
        } else {
            return requestFloor.number() <= currentFloor.number();
        }
    }
}
