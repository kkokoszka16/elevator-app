package com.elevator.application.port.in;

import com.elevator.domain.model.Direction;
import com.elevator.domain.model.Floor;

public interface CallElevatorUseCase {

    void callElevator(Floor floor, Direction direction);
}
