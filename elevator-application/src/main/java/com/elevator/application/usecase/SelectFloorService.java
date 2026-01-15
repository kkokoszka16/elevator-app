package com.elevator.application.usecase;

import com.elevator.application.port.in.SelectFloorUseCase;
import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.event.FloorRequestedEvent;
import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.domain.model.RequestSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SelectFloorService implements SelectFloorUseCase {

    private final ElevatorRepository elevatorRepository;
    private final BuildingConfigRepository configRepository;
    private final ElevatorEventPublisher eventPublisher;

    @Override
    public void selectFloor(ElevatorId elevatorId, Floor floor) {
        var config = configRepository.get();

        if (!config.isValidFloor(floor)) {
            throw new InvalidFloorException(floor.number());
        }

        var elevator = elevatorRepository.findById(elevatorId)
                .orElseThrow(() -> new ElevatorNotFoundException(elevatorId));

        elevator.addDestination(floor);
        elevatorRepository.save(elevator);

        eventPublisher.publish(new FloorRequestedEvent(elevatorId, floor, RequestSource.CAB_CALL));
        eventPublisher.publishStateUpdate(elevatorRepository.findAll());
    }
}
