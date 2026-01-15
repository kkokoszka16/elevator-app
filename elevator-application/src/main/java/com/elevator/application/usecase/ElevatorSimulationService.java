package com.elevator.application.usecase;

import com.elevator.application.port.in.ElevatorSimulationUseCase;
import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.event.DoorStateChangedEvent;
import com.elevator.domain.event.ElevatorArrivedEvent;
import com.elevator.domain.event.ElevatorMovedEvent;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.DoorState;
import com.elevator.domain.model.Elevator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class ElevatorSimulationService implements ElevatorSimulationUseCase {

    private final ElevatorRepository elevatorRepository;
    private final BuildingConfigRepository configRepository;
    private final ElevatorEventPublisher eventPublisher;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Map<Integer, Integer> doorTimers = new ConcurrentHashMap<>();

    @Override
    public void tick() {
        if (!running.get()) {
            return;
        }

        var elevators = elevatorRepository.findAll();
        var stateChanged = false;

        for (var elevator : elevators) {
            var changed = processElevator(elevator);
            if (changed) {
                elevatorRepository.save(elevator);
                stateChanged = true;
            }
        }

        if (stateChanged) {
            eventPublisher.publishStateUpdate(elevatorRepository.findAll());
        }
    }

    @Override
    public void start() {
        running.set(true);
    }

    @Override
    public void stop() {
        running.set(false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private boolean processElevator(Elevator elevator) {
        var state = elevator.getState();

        return switch (state) {
            case IDLE -> processIdleElevator(elevator);
            case MOVING -> processMovingElevator(elevator);
            case STOPPED -> processStoppedElevator(elevator);
            case DOOR_OPENING -> processDoorOpeningElevator(elevator);
            case DOOR_OPEN -> processDoorOpenElevator(elevator);
            case DOOR_CLOSING -> processDoorClosingElevator(elevator);
        };
    }

    private boolean processIdleElevator(Elevator elevator) {
        if (elevator.hasDestinations()) {
            var nextDest = elevator.getNextDestination();
            if (nextDest != null) {
                if (nextDest.equals(elevator.getCurrentFloor())) {
                    elevator.openDoors();
                    eventPublisher.publish(new DoorStateChangedEvent(
                            elevator.getId(), DoorState.CLOSED, DoorState.OPENING));
                    return true;
                }
                moveElevatorTowardsDestination(elevator, nextDest);
                return true;
            }
        }
        return false;
    }

    private boolean processMovingElevator(Elevator elevator) {
        if (elevator.shouldStopAtCurrentFloor()) {
            elevator.stop();
            eventPublisher.publish(new ElevatorArrivedEvent(elevator.getId(), elevator.getCurrentFloor()));
            elevator.openDoors();
            eventPublisher.publish(new DoorStateChangedEvent(
                    elevator.getId(), DoorState.CLOSED, DoorState.OPENING));
            return true;
        }

        var nextDest = elevator.getNextDestination();
        if (nextDest == null) {
            elevator.setIdle();
            return true;
        }

        var previousFloor = elevator.getCurrentFloor();
        moveElevatorTowardsDestination(elevator, nextDest);

        eventPublisher.publish(new ElevatorMovedEvent(
                elevator.getId(),
                previousFloor,
                elevator.getCurrentFloor(),
                elevator.getDirection()));

        if (elevator.shouldStopAtCurrentFloor()) {
            elevator.stop();
            eventPublisher.publish(new ElevatorArrivedEvent(elevator.getId(), elevator.getCurrentFloor()));
        }

        return true;
    }

    private boolean processStoppedElevator(Elevator elevator) {
        elevator.openDoors();
        eventPublisher.publish(new DoorStateChangedEvent(
                elevator.getId(), DoorState.CLOSED, DoorState.OPENING));
        return true;
    }

    private boolean processDoorOpeningElevator(Elevator elevator) {
        elevator.doorsOpened();
        doorTimers.put(elevator.getId().value(), 3);
        eventPublisher.publish(new DoorStateChangedEvent(
                elevator.getId(), DoorState.OPENING, DoorState.OPEN));
        return true;
    }

    private boolean processDoorOpenElevator(Elevator elevator) {
        var timer = doorTimers.getOrDefault(elevator.getId().value(), 0);
        if (timer > 0) {
            doorTimers.put(elevator.getId().value(), timer - 1);
            return false;
        }

        doorTimers.remove(elevator.getId().value());
        elevator.closeDoors();
        eventPublisher.publish(new DoorStateChangedEvent(
                elevator.getId(), DoorState.OPEN, DoorState.CLOSING));
        return true;
    }

    private boolean processDoorClosingElevator(Elevator elevator) {
        elevator.doorsClosed();
        eventPublisher.publish(new DoorStateChangedEvent(
                elevator.getId(), DoorState.CLOSING, DoorState.CLOSED));

        if (!elevator.hasDestinations()) {
            elevator.setIdle();
        }

        return true;
    }

    private void moveElevatorTowardsDestination(Elevator elevator, com.elevator.domain.model.Floor destination) {
        if (destination.isAbove(elevator.getCurrentFloor())) {
            elevator.moveUp();
        } else if (destination.isBelow(elevator.getCurrentFloor())) {
            elevator.moveDown();
        }
    }
}
