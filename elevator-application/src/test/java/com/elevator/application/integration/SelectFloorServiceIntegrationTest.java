package com.elevator.application.integration;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.application.usecase.SelectFloorService;
import com.elevator.domain.event.FloorRequestedEvent;
import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.DoorState;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.ElevatorState;
import com.elevator.domain.model.Floor;
import com.elevator.domain.model.RequestSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

@DisplayName("SelectFloorService Integration Tests")
class SelectFloorServiceIntegrationTest {

    private ElevatorRepository elevatorRepository;
    private BuildingConfigRepository configRepository;
    private ElevatorEventPublisher eventPublisher;
    private SelectFloorService selectFloorService;

    private BuildingConfig defaultConfig;

    @BeforeEach
    void setUp() {
        elevatorRepository = mock(ElevatorRepository.class);
        configRepository = mock(BuildingConfigRepository.class);
        eventPublisher = mock(ElevatorEventPublisher.class);

        selectFloorService = new SelectFloorService(
                elevatorRepository,
                configRepository,
                eventPublisher
        );

        defaultConfig = new BuildingConfig(10, 3,
                java.time.Duration.ofSeconds(3), java.time.Duration.ofSeconds(2));
        given(configRepository.get()).willReturn(defaultConfig);
    }

    @Test
    @DisplayName("should add destination to elevator and publish cab call event")
    void should_add_destination_to_elevator_and_publish_cab_call_event() {
        var elevatorId = new ElevatorId(1);
        var floor = new Floor(7);
        var elevator = createElevator(1, 3);

        given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

        selectFloorService.selectFloor(elevatorId, floor);

        var elevatorCaptor = ArgumentCaptor.forClass(Elevator.class);
        then(elevatorRepository).should().save(elevatorCaptor.capture());

        var savedElevator = elevatorCaptor.getValue();
        assertThat(savedElevator.getDestinations()).contains(floor);

        var eventCaptor = ArgumentCaptor.forClass(FloorRequestedEvent.class);
        then(eventPublisher).should().publish(eventCaptor.capture());

        var event = eventCaptor.getValue();
        assertThat(event.elevatorId()).isEqualTo(elevatorId);
        assertThat(event.floor()).isEqualTo(floor);
        assertThat(event.source()).isEqualTo(RequestSource.CAB_CALL);
    }

    @ParameterizedTest(name = "{index}: floor={0}")
    @ValueSource(ints = {0, 5, 9})
    @DisplayName("should handle valid floor selections")
    void should_handle_valid_floor_selections(int floorNumber) {
        var elevatorId = new ElevatorId(1);
        var floor = new Floor(floorNumber);
        var elevator = createElevator(1, 5);

        given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

        selectFloorService.selectFloor(elevatorId, floor);

        then(elevatorRepository).should().save(any(Elevator.class));
        then(eventPublisher).should().publish(any(FloorRequestedEvent.class));
        then(eventPublisher).should().publishStateUpdate(any());
    }

    @Test
    @DisplayName("should throw ElevatorNotFoundException when elevator does not exist")
    void should_throw_ElevatorNotFoundException_when_elevator_does_not_exist() {
        var elevatorId = new ElevatorId(99);
        var floor = new Floor(5);

        given(elevatorRepository.findById(elevatorId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> selectFloorService.selectFloor(elevatorId, floor))
                .isInstanceOf(ElevatorNotFoundException.class)
                .hasMessageContaining("99");

        then(elevatorRepository).should(times(0)).save(any());
        then(eventPublisher).should(times(0)).publish(any());
    }

    @Test
    @DisplayName("should throw InvalidFloorException for floor above max")
    void should_throw_InvalidFloorException_for_floor_above_max() {
        var elevatorId = new ElevatorId(1);
        var elevator = createElevator(1, 5);

        given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

        assertThatThrownBy(() -> {
            var invalidFloor = new Floor(10);
            selectFloorService.selectFloor(elevatorId, invalidFloor);
        }).isInstanceOf(InvalidFloorException.class);
    }

    @Test
    @DisplayName("should throw InvalidFloorException for negative floor")
    void should_throw_InvalidFloorException_for_negative_floor() {
        assertThatThrownBy(() -> {
            var invalidFloor = new Floor(-1);
            selectFloorService.selectFloor(new ElevatorId(1), invalidFloor);
        }).isInstanceOf(InvalidFloorException.class);
    }

    @Test
    @DisplayName("should add multiple destinations to same elevator")
    void should_add_multiple_destinations_to_same_elevator() {
        var elevatorId = new ElevatorId(1);
        var elevator = createElevator(1, 1);

        given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

        selectFloorService.selectFloor(elevatorId, new Floor(3));
        selectFloorService.selectFloor(elevatorId, new Floor(5));
        selectFloorService.selectFloor(elevatorId, new Floor(8));

        then(elevatorRepository).should(times(3)).save(any(Elevator.class));
        then(eventPublisher).should(times(3)).publish(any(FloorRequestedEvent.class));
    }

    @Test
    @DisplayName("should not add duplicate destination")
    void should_not_add_duplicate_destination() {
        var elevatorId = new ElevatorId(1);
        var floor = new Floor(7);
        var elevator = createElevator(1, 3);
        elevator.addDestination(floor);

        given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

        selectFloorService.selectFloor(elevatorId, floor);

        var captor = ArgumentCaptor.forClass(Elevator.class);
        then(elevatorRepository).should().save(captor.capture());

        var savedElevator = captor.getValue();
        var destinationCount = savedElevator.getDestinations().stream()
                .filter(f -> f.equals(floor))
                .count();

        assertThat(destinationCount).isEqualTo(1);
    }

    @Test
    @DisplayName("should publish state update after floor selection")
    void should_publish_state_update_after_floor_selection() {
        var elevatorId = new ElevatorId(1);
        var floor = new Floor(5);
        var elevator1 = createElevator(1, 3);
        var elevator2 = createElevator(2, 7);

        given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator1));
        given(elevatorRepository.findAll()).willReturn(List.of(elevator1, elevator2));

        selectFloorService.selectFloor(elevatorId, floor);

        var captor = ArgumentCaptor.forClass(List.class);
        then(eventPublisher).should().publishStateUpdate(captor.capture());

        var elevators = (List<Elevator>) captor.getValue();
        assertThat(elevators).hasSize(2);
    }

    @Test
    @DisplayName("should preserve existing destinations when adding new one")
    void should_preserve_existing_destinations_when_adding_new_one() {
        var elevatorId = new ElevatorId(1);
        var newFloor = new Floor(8);
        var elevator = createElevator(1, 2);
        elevator.addDestination(new Floor(4));
        elevator.addDestination(new Floor(6));

        given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

        selectFloorService.selectFloor(elevatorId, newFloor);

        var captor = ArgumentCaptor.forClass(Elevator.class);
        then(elevatorRepository).should().save(captor.capture());

        var savedElevator = captor.getValue();
        assertThat(savedElevator.getDestinations())
                .hasSize(3)
                .contains(new Floor(4), new Floor(6), new Floor(8));
    }

    private Elevator createElevator(int id, int currentFloor) {
        return new Elevator(new ElevatorId(id), 10);
    }
}
