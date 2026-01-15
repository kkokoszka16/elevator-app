package com.elevator.domain.model;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@Getter
public class Elevator {

    private final ElevatorId id;
    private Floor currentFloor;
    private Direction direction;
    private DoorState doorState;
    private ElevatorState state;
    private final Set<Floor> destinationFloors;
    private final int maxFloor;

    public Elevator(ElevatorId id, int maxFloor) {
        this.id = id;
        this.maxFloor = maxFloor;
        this.currentFloor = new Floor(0);
        this.direction = Direction.IDLE;
        this.doorState = DoorState.CLOSED;
        this.state = ElevatorState.IDLE;
        this.destinationFloors = new TreeSet<>();
    }

    public void addDestination(Floor floor) {
        if (floor.number() > maxFloor) {
            throw new IllegalArgumentException("Floor exceeds building height");
        }
        if (!floor.equals(currentFloor)) {
            destinationFloors.add(floor);
            if (direction == Direction.IDLE) {
                updateDirectionBasedOnDestinations();
            } else if (findNextDestinationInCurrentDirection() == null) {
                updateDirectionBasedOnDestinations();
            }
        }
    }

    public void removeDestination(Floor floor) {
        destinationFloors.remove(floor);
    }

    public boolean hasDestination(Floor floor) {
        return destinationFloors.contains(floor);
    }

    public Set<Floor> getDestinations() {
        return Collections.unmodifiableSet(destinationFloors);
    }

    public boolean hasDestinations() {
        return !destinationFloors.isEmpty();
    }

    public void moveUp() {
        if (!doorState.canMove()) {
            throw new IllegalStateException("Cannot move while doors are not closed");
        }
        if (currentFloor.number() >= maxFloor) {
            throw new IllegalStateException("Already at top floor");
        }
        this.currentFloor = currentFloor.above();
        this.direction = Direction.UP;
        this.state = ElevatorState.MOVING;
    }

    public void moveDown() {
        if (!doorState.canMove()) {
            throw new IllegalStateException("Cannot move while doors are not closed");
        }
        if (currentFloor.number() <= 0) {
            throw new IllegalStateException("Already at ground floor");
        }
        this.currentFloor = currentFloor.below();
        this.direction = Direction.DOWN;
        this.state = ElevatorState.MOVING;
    }

    public void stop() {
        this.state = ElevatorState.STOPPED;
    }

    public void openDoors() {
        this.doorState = DoorState.OPENING;
        this.state = ElevatorState.DOOR_OPENING;
    }

    public void doorsOpened() {
        this.doorState = DoorState.OPEN;
        this.state = ElevatorState.DOOR_OPEN;
        removeDestination(currentFloor);
    }

    public void closeDoors() {
        this.doorState = DoorState.CLOSING;
        this.state = ElevatorState.DOOR_CLOSING;
    }

    public void doorsClosed() {
        this.doorState = DoorState.CLOSED;
        if (hasDestinations()) {
            updateDirectionBasedOnDestinations();
            this.state = ElevatorState.MOVING;
        } else {
            this.direction = Direction.IDLE;
            this.state = ElevatorState.IDLE;
        }
    }

    public void setIdle() {
        this.direction = Direction.IDLE;
        this.state = ElevatorState.IDLE;
    }

    public boolean shouldStopAtCurrentFloor() {
        return destinationFloors.contains(currentFloor);
    }

    public boolean isIdle() {
        return state.isIdle() && !hasDestinations();
    }

    public boolean isMovingTowards(Floor floor) {
        if (direction == Direction.IDLE) {
            return false;
        }
        if (direction == Direction.UP) {
            return floor.isAbove(currentFloor) || floor.equals(currentFloor);
        }
        return floor.isBelow(currentFloor) || floor.equals(currentFloor);
    }

    public int distanceTo(Floor floor) {
        return currentFloor.distanceTo(floor);
    }

    public boolean canServiceRequest(Floor floor, Direction requestDirection) {
        if (isIdle()) {
            return true;
        }
        if (direction == Direction.IDLE) {
            return true;
        }
        if (direction == requestDirection) {
            if (direction == Direction.UP) {
                return floor.number() >= currentFloor.number();
            } else {
                return floor.number() <= currentFloor.number();
            }
        }
        return false;
    }

    private void updateDirectionIfIdle() {
        if (direction == Direction.IDLE && !destinationFloors.isEmpty()) {
            updateDirectionBasedOnDestinations();
        }
    }

    private void updateDirectionBasedOnDestinations() {
        var nextDestination = findNextDestinationInCurrentDirection();
        if (nextDestination == null) {
            reverseDirection();
            nextDestination = findNextDestinationInCurrentDirection();
        }
        if (nextDestination != null) {
            if (nextDestination.isAbove(currentFloor)) {
                direction = Direction.UP;
            } else if (nextDestination.isBelow(currentFloor)) {
                direction = Direction.DOWN;
            }
        } else if (!destinationFloors.isEmpty()) {
            var anyDestination = destinationFloors.iterator().next();
            if (anyDestination.isAbove(currentFloor)) {
                direction = Direction.UP;
            } else if (anyDestination.isBelow(currentFloor)) {
                direction = Direction.DOWN;
            }
        }
    }

    private Floor findNextDestinationInCurrentDirection() {
        if (direction == Direction.UP || direction == Direction.IDLE) {
            return destinationFloors.stream()
                    .filter(f -> f.number() >= currentFloor.number())
                    .min(Floor::compareTo)
                    .orElse(null);
        } else {
            return destinationFloors.stream()
                    .filter(f -> f.number() <= currentFloor.number())
                    .max(Floor::compareTo)
                    .orElse(null);
        }
    }

    private void reverseDirection() {
        direction = direction.opposite();
    }

    public Floor getNextDestination() {
        if (destinationFloors.isEmpty()) {
            return null;
        }
        if (direction == Direction.IDLE) {
            updateDirectionBasedOnDestinations();
        }
        var next = findNextDestinationInCurrentDirection();
        if (next == null && direction != Direction.IDLE) {
            reverseDirection();
            next = findNextDestinationInCurrentDirection();
        }
        return next;
    }
}
