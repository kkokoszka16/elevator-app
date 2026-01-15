package com.elevator.application.integration;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.application.usecase.CallElevatorService;
import com.elevator.domain.event.FloorRequestedEvent;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.exception.NoAvailableElevatorException;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.DoorState;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.ElevatorState;
import com.elevator.domain.model.Floor;
import com.elevator.domain.service.LookSchedulingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@DisplayName("CallElevatorService Integration Tests")
class CallElevatorServiceIntegrationTest {

    private ElevatorRepository elevatorRepository;
    private BuildingConfigRepository configRepository;
    private ElevatorEventPublisher eventPublisher;
    private CallElevatorService callElevatorService;

    private BuildingConfig defaultConfig;

    @BeforeEach
    void setUp() {
        elevatorRepository = mock(ElevatorRepository.class);
        configRepository = mock(BuildingConfigRepository.class);
        eventPublisher = mock(ElevatorEventPublisher.class);

        var schedulingStrategy = new LookSchedulingStrategy();
        callElevatorService = new CallElevatorService(
                elevatorRepository,
                configRepository,
                schedulingStrategy,
                eventPublisher
        );

        defaultConfig = new BuildingConfig(10, 3,
                java.time.Duration.ofSeconds(3), java.time.Duration.ofSeconds(2));
        given(configRepository.get()).willReturn(defaultConfig);
    }

    @Test
    @DisplayName("should select elevator and publish events")
    void should_select_elevator_and_publish_events() {
        var floor = new Floor(5);
        var direction = Direction.UP;

        var elevator1 = createIdleElevator(1, 0);
        var elevator2 = createIdleElevator(2, 0);
        var elevator3 = createIdleElevator(3, 0);

        given(elevatorRepository.findAll()).willReturn(List.of(elevator1, elevator2, elevator3));
        given(elevatorRepository.findById(any())).willReturn(Optional.of(elevator1));

        callElevatorService.callElevator(floor, direction);

        var elevatorCaptor = ArgumentCaptor.forClass(Elevator.class);
        then(elevatorRepository).should().save(elevatorCaptor.capture());

        var savedElevator = elevatorCaptor.getValue();
        assertThat(savedElevator.getDestinations()).contains(floor);

        var eventCaptor = ArgumentCaptor.forClass(FloorRequestedEvent.class);
        then(eventPublisher).should().publish(eventCaptor.capture());
        then(eventPublisher).should().publishStateUpdate(any());

        var event = eventCaptor.getValue();
        assertThat(event.floor()).isEqualTo(floor);
    }

    @ParameterizedTest(name = "{index}: floor={0}, direction={1}")
    @CsvSource({
            "0, UP",
            "9, DOWN",
            "5, UP",
            "7, DOWN"
    })
    @DisplayName("should handle various floor and direction combinations")
    void should_handle_various_floor_and_direction_combinations(int floorNumber, String directionStr) {
        var floor = new Floor(floorNumber);
        var direction = Direction.valueOf(directionStr);

        var elevator = createIdleElevator(1, 5);
        given(elevatorRepository.findAll()).willReturn(List.of(elevator));
        given(elevatorRepository.findById(new ElevatorId(1))).willReturn(Optional.of(elevator));

        callElevatorService.callElevator(floor, direction);

        then(elevatorRepository).should().save(any(Elevator.class));
        then(eventPublisher).should().publish(any(FloorRequestedEvent.class));
    }

    @Test
    @DisplayName("should throw InvalidFloorException when floor exceeds building limit")
    void should_throw_InvalidFloorException_when_floor_exceeds_building_limit() {
        var invalidFloor = new Floor(15);
        var direction = Direction.UP;

        assertThatThrownBy(() -> callElevatorService.callElevator(invalidFloor, direction))
                .isInstanceOf(InvalidFloorException.class)
                .hasMessageContaining("15");

        then(elevatorRepository).should(times(0)).save(any());
        then(eventPublisher).should(times(0)).publish(any());
    }

    @Test
    @DisplayName("should throw InvalidFloorException when floor is below ground")
    void should_throw_InvalidFloorException_when_floor_is_below_ground() {
        assertThatThrownBy(() -> {
            var invalidFloor = new Floor(-1);
            callElevatorService.callElevator(invalidFloor, Direction.UP);
        }).isInstanceOf(InvalidFloorException.class);
    }

    @Test
    @DisplayName("should throw NoAvailableElevatorException when no elevators in system")
    void should_throw_NoAvailableElevatorException_when_no_elevators_in_system() {
        var floor = new Floor(5);
        var direction = Direction.UP;

        given(elevatorRepository.findAll()).willReturn(List.of());

        assertThatThrownBy(() -> callElevatorService.callElevator(floor, direction))
                .isInstanceOf(NoAvailableElevatorException.class)
                .hasMessageContaining("5");
    }

    @Test
    @DisplayName("should handle elevator selection with destinations")
    void should_handle_elevator_selection_with_destinations() {
        var floor = new Floor(5);
        var direction = Direction.UP;

        var elevator1 = createIdleElevator(1, 0);
        elevator1.addDestination(new Floor(7));

        given(elevatorRepository.findAll()).willReturn(List.of(elevator1));
        given(elevatorRepository.findById(any())).willReturn(Optional.of(elevator1));

        callElevatorService.callElevator(floor, direction);

        var captor = ArgumentCaptor.forClass(Elevator.class);
        then(elevatorRepository).should().save(captor.capture());

        assertThat(captor.getValue().getDestinations()).contains(floor, new Floor(7));
    }

    @Test
    @DisplayName("should add destination to elevator with existing destinations")
    void should_add_destination_to_elevator_with_existing_destinations() {
        var floor = new Floor(7);
        var direction = Direction.UP;

        var elevator = createIdleElevator(1, 3);
        elevator.addDestination(new Floor(5));
        elevator.addDestination(new Floor(8));

        given(elevatorRepository.findAll()).willReturn(List.of(elevator));
        given(elevatorRepository.findById(new ElevatorId(1))).willReturn(Optional.of(elevator));

        callElevatorService.callElevator(floor, direction);

        var captor = ArgumentCaptor.forClass(Elevator.class);
        then(elevatorRepository).should().save(captor.capture());

        var savedElevator = captor.getValue();
        assertThat(savedElevator.getDestinations())
                .hasSize(3)
                .contains(new Floor(5), new Floor(7), new Floor(8));
    }

    @Test
    @DisplayName("should publish state update with all elevators after call")
    void should_publish_state_update_with_all_elevators_after_call() {
        var floor = new Floor(5);
        var direction = Direction.UP;

        var elevator1 = createIdleElevator(1, 3);
        var elevator2 = createIdleElevator(2, 7);

        given(elevatorRepository.findAll()).willReturn(List.of(elevator1, elevator2));
        given(elevatorRepository.findById(any())).willReturn(Optional.of(elevator1));

        callElevatorService.callElevator(floor, direction);

        var captor = ArgumentCaptor.forClass(List.class);
        then(eventPublisher).should().publishStateUpdate(captor.capture());

        var publishedElevators = (List<Elevator>) captor.getValue();
        assertThat(publishedElevators).hasSize(2);
    }

    private Elevator createIdleElevator(int id, int currentFloor) {
        return new Elevator(new ElevatorId(id), 10);
    }

    private Elevator createMovingElevator(int id, int currentFloor, Direction direction) {
        var elevator = new Elevator(new ElevatorId(id), 10);
        var targetFloor = currentFloor + (direction == Direction.UP ? 3 : -3);
        if (targetFloor >= 0 && targetFloor < 10) {
            elevator.addDestination(new Floor(targetFloor));
        }
        return elevator;
    }
}
