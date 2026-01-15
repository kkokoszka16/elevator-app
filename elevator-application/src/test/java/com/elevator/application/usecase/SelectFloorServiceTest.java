package com.elevator.application.usecase;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.event.FloorRequestedEvent;
import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.domain.model.RequestSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SelectFloorServiceTest {

    @Mock
    private ElevatorRepository elevatorRepository;

    @Mock
    private BuildingConfigRepository configRepository;

    @Mock
    private ElevatorEventPublisher eventPublisher;

    private SelectFloorService service;

    private static final BuildingConfig DEFAULT_CONFIG = new BuildingConfig(
            10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2)
    );

    @BeforeEach
    void setUp() {
        service = new SelectFloorService(
                elevatorRepository,
                configRepository,
                eventPublisher
        );
    }

    @Nested
    class SuccessfulSelection {

        @Test
        void should_add_destination_when_valid_request() {
            // given
            var elevatorId = new ElevatorId(0);
            var floor = new Floor(5);
            var elevator = new Elevator(elevatorId, 9);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.selectFloor(elevatorId, floor);

            // then
            then(elevatorRepository).should().save(elevator);
            assertThat(elevator.hasDestination(floor)).isTrue();
        }

        @Test
        void should_publish_event_after_selection() {
            // given
            var elevatorId = new ElevatorId(0);
            var floor = new Floor(5);
            var elevator = new Elevator(elevatorId, 9);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.selectFloor(elevatorId, floor);

            // then
            var eventCaptor = ArgumentCaptor.forClass(FloorRequestedEvent.class);
            then(eventPublisher).should().publish(eventCaptor.capture());

            var event = eventCaptor.getValue();
            assertThat(event.elevatorId()).isEqualTo(elevatorId);
            assertThat(event.floor()).isEqualTo(floor);
            assertThat(event.source()).isEqualTo(RequestSource.CAB_CALL);
        }

        @Test
        void should_publish_state_update_after_selection() {
            // given
            var elevatorId = new ElevatorId(0);
            var floor = new Floor(5);
            var elevator = new Elevator(elevatorId, 9);
            var allElevators = List.of(elevator);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));
            given(elevatorRepository.findAll()).willReturn(allElevators);

            // when
            service.selectFloor(elevatorId, floor);

            // then
            then(eventPublisher).should().publishStateUpdate(allElevators);
        }

        @ParameterizedTest(name = "should select floor {0}")
        @ValueSource(ints = {0, 1, 5, 9})
        void should_select_various_valid_floors(int floorNum) {
            // given
            var elevatorId = new ElevatorId(0);
            var floor = new Floor(floorNum);
            var elevator = new Elevator(elevatorId, 9);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.selectFloor(elevatorId, floor);

            // then
            then(elevatorRepository).should().save(elevator);
        }
    }

    @Nested
    class InvalidFloor {

        @Test
        void should_throw_exception_when_floor_exceeds_building() {
            // given
            var elevatorId = new ElevatorId(0);
            var floor = new Floor(15);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);

            // when / then
            assertThatThrownBy(() -> service.selectFloor(elevatorId, floor))
                    .isInstanceOf(InvalidFloorException.class);

            then(elevatorRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publish(any());
        }

        @Test
        void should_throw_exception_when_floor_equals_building_height() {
            // given
            var elevatorId = new ElevatorId(0);
            var floor = new Floor(10);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);

            // when / then
            assertThatThrownBy(() -> service.selectFloor(elevatorId, floor))
                    .isInstanceOf(InvalidFloorException.class);
        }
    }

    @Nested
    class ElevatorNotFound {

        @Test
        void should_throw_exception_when_elevator_not_found() {
            // given
            var elevatorId = new ElevatorId(99);
            var floor = new Floor(5);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> service.selectFloor(elevatorId, floor))
                    .isInstanceOf(ElevatorNotFoundException.class);

            then(elevatorRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publish(any());
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void should_handle_selection_at_current_floor() {
            // given
            var elevatorId = new ElevatorId(0);
            var floor = new Floor(0);
            var elevator = new Elevator(elevatorId, 9);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.selectFloor(elevatorId, floor);

            // then
            then(elevatorRepository).should().save(elevator);
            assertThat(elevator.hasDestination(floor)).isFalse();
        }

        @Test
        void should_handle_multiple_selections_same_floor() {
            // given
            var elevatorId = new ElevatorId(0);
            var floor = new Floor(5);
            var elevator = new Elevator(elevatorId, 9);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.selectFloor(elevatorId, floor);
            service.selectFloor(elevatorId, floor);

            // then
            assertThat(elevator.getDestinations()).hasSize(1);
        }
    }
}
