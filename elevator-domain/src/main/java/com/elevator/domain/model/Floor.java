package com.elevator.domain.model;

import com.elevator.domain.exception.InvalidFloorException;

public record Floor(int number) implements Comparable<Floor> {

    public Floor {
        if (number < 0) {
            throw new InvalidFloorException(number);
        }
    }

    public Floor above() {
        return new Floor(number + 1);
    }

    public Floor below() {
        if (number == 0) {
            throw new InvalidFloorException(-1);
        }
        return new Floor(number - 1);
    }

    public int distanceTo(Floor other) {
        return Math.abs(this.number - other.number);
    }

    public boolean isAbove(Floor other) {
        return this.number > other.number;
    }

    public boolean isBelow(Floor other) {
        return this.number < other.number;
    }

    @Override
    public int compareTo(Floor other) {
        return Integer.compare(this.number, other.number);
    }
}
