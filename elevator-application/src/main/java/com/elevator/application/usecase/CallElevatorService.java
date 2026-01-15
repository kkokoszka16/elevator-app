package com.elevator.application.usecase;

import com.elevator.application.port.in.CallElevatorUseCase;
import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.event.FloorRequestedEvent;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.exception.NoAvailableElevatorException;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.Floor;
import com.elevator.domain.model.RequestSource;
import com.elevator.domain.service.ElevatorSchedulingStrategy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CallElevatorService implements CallElevatorUseCase {

    private final ElevatorRepository elevatorRepository;
    private final BuildingConfigRepository configRepository;
    private final ElevatorSchedulingStrategy schedulingStrategy;
    private final ElevatorEventPublisher eventPublisher;

    @Override
    public void callElevator(Floor floor, Direction direction) {
        var config = configRepository.get();

        if (!config.isValidFloor(floor)) {
            throw new InvalidFloorException(floor.number());
        }

        var elevators = elevatorRepository.findAll();
        var selectedElevatorId = schedulingStrategy.selectElevator(floor, direction, elevators)
                .orElseThrow(() -> new NoAvailableElevatorException(floor));

        var elevator = elevatorRepository.findById(selectedElevatorId)
                .orElseThrow(() -> new NoAvailableElevatorException(floor));

        elevator.addDestination(floor);
        elevatorRepository.save(elevator);

        eventPublisher.publish(new FloorRequestedEvent(selectedElevatorId, floor, RequestSource.HALL_CALL));
        eventPublisher.publishStateUpdate(elevatorRepository.findAll());
    }
}
