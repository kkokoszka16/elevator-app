package com.elevator.application.usecase;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.event.FloorRequestedEvent;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.exception.NoAvailableElevatorException;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.domain.model.RequestSource;
import com.elevator.domain.service.ElevatorSchedulingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CallElevatorServiceTest {

    @Mock
    private ElevatorRepository elevatorRepository;

    @Mock
    private BuildingConfigRepository configRepository;

    @Mock
    private ElevatorSchedulingStrategy schedulingStrategy;

    @Mock
    private ElevatorEventPublisher eventPublisher;

    private CallElevatorService service;

    private static final BuildingConfig DEFAULT_CONFIG = new BuildingConfig(
            10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2)
    );

    @BeforeEach
    void setUp() {
        service = new CallElevatorService(
                elevatorRepository,
                configRepository,
                schedulingStrategy,
                eventPublisher
        );
    }

    @Nested
    class SuccessfulCall {

        @Test
        void should_call_elevator_when_valid_request() {
            // given
            var floor = new Floor(5);
            var direction = Direction.UP;
            var elevatorId = new ElevatorId(0);
            var elevator = new Elevator(elevatorId, 9);
            var elevators = List.of(elevator);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findAll()).willReturn(elevators);
            given(schedulingStrategy.selectElevator(floor, direction, elevators))
                    .willReturn(Optional.of(elevatorId));
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.callElevator(floor, direction);

            // then
            then(elevatorRepository).should().save(elevator);
            assertThat(elevator.hasDestination(floor)).isTrue();
        }

        @Test
        void should_publish_event_after_call() {
            // given
            var floor = new Floor(5);
            var direction = Direction.UP;
            var elevatorId = new ElevatorId(0);
            var elevator = new Elevator(elevatorId, 9);
            var elevators = List.of(elevator);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findAll()).willReturn(elevators);
            given(schedulingStrategy.selectElevator(floor, direction, elevators))
                    .willReturn(Optional.of(elevatorId));
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.callElevator(floor, direction);

            // then
            var eventCaptor = ArgumentCaptor.forClass(FloorRequestedEvent.class);
            then(eventPublisher).should().publish(eventCaptor.capture());

            var event = eventCaptor.getValue();
            assertThat(event.elevatorId()).isEqualTo(elevatorId);
            assertThat(event.floor()).isEqualTo(floor);
            assertThat(event.source()).isEqualTo(RequestSource.HALL_CALL);
        }

        @Test
        void should_publish_state_update_after_call() {
            // given
            var floor = new Floor(5);
            var direction = Direction.UP;
            var elevatorId = new ElevatorId(0);
            var elevator = new Elevator(elevatorId, 9);
            var elevators = List.of(elevator);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findAll()).willReturn(elevators);
            given(schedulingStrategy.selectElevator(floor, direction, elevators))
                    .willReturn(Optional.of(elevatorId));
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.callElevator(floor, direction);

            // then
            then(eventPublisher).should().publishStateUpdate(any());
        }

        @ParameterizedTest(name = "should call elevator at floor {0} going {1}")
        @CsvSource({
                "0, UP",
                "5, UP",
                "9, DOWN",
                "3, DOWN"
        })
        void should_call_elevator_for_various_floors_and_directions(int floorNum, Direction direction) {
            // given
            var floor = new Floor(floorNum);
            var elevatorId = new ElevatorId(0);
            var elevator = new Elevator(elevatorId, 9);
            var elevators = List.of(elevator);

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findAll()).willReturn(elevators);
            given(schedulingStrategy.selectElevator(eq(floor), eq(direction), any()))
                    .willReturn(Optional.of(elevatorId));
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            service.callElevator(floor, direction);

            // then
            then(elevatorRepository).should().save(elevator);
        }
    }

    @Nested
    class InvalidFloor {

        @Test
        void should_throw_exception_when_floor_exceeds_building() {
            // given
            var floor = new Floor(15);
            var direction = Direction.UP;

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);

            // when / then
            assertThatThrownBy(() -> service.callElevator(floor, direction))
                    .isInstanceOf(InvalidFloorException.class);

            then(elevatorRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publish(any());
        }

        @Test
        void should_throw_exception_when_floor_equals_building_height() {
            // given
            var floor = new Floor(10);
            var direction = Direction.UP;

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);

            // when / then
            assertThatThrownBy(() -> service.callElevator(floor, direction))
                    .isInstanceOf(InvalidFloorException.class);
        }
    }

    @Nested
    class NoAvailableElevator {

        @Test
        void should_throw_exception_when_no_elevator_available() {
            // given
            var floor = new Floor(5);
            var direction = Direction.UP;
            var elevators = List.<Elevator>of();

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findAll()).willReturn(elevators);
            given(schedulingStrategy.selectElevator(floor, direction, elevators))
                    .willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> service.callElevator(floor, direction))
                    .isInstanceOf(NoAvailableElevatorException.class);

            then(elevatorRepository).should(never()).save(any());
        }

        @Test
        void should_throw_exception_when_selected_elevator_not_found() {
            // given
            var floor = new Floor(5);
            var direction = Direction.UP;
            var elevatorId = new ElevatorId(0);
            var elevators = List.<Elevator>of();

            given(configRepository.get()).willReturn(DEFAULT_CONFIG);
            given(elevatorRepository.findAll()).willReturn(elevators);
            given(schedulingStrategy.selectElevator(floor, direction, elevators))
                    .willReturn(Optional.of(elevatorId));
            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> service.callElevator(floor, direction))
                    .isInstanceOf(NoAvailableElevatorException.class);
        }
    }
}
