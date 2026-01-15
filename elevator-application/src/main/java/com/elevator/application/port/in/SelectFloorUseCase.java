package com.elevator.application.port.in;

import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;

public interface SelectFloorUseCase {

    void selectFloor(ElevatorId elevatorId, Floor floor);
}
