package com.elevator.application.usecase;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.ElevatorState;
import com.elevator.domain.model.Floor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ElevatorSimulationServiceTest {

    @Mock
    private ElevatorRepository elevatorRepository;

    @Mock
    private BuildingConfigRepository configRepository;

    @Mock
    private ElevatorEventPublisher eventPublisher;

    private ElevatorSimulationService service;

    private static final BuildingConfig DEFAULT_CONFIG = new BuildingConfig(
            10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2)
    );

    @BeforeEach
    void setUp() {
        service = new ElevatorSimulationService(
                elevatorRepository,
                configRepository,
                eventPublisher
        );
    }

    @Nested
    class StartStop {

        @Test
        void should_not_be_running_initially() {
            // then
            assertThat(service.isRunning()).isFalse();
        }

        @Test
        void should_be_running_after_start() {
            // when
            service.start();

            // then
            assertThat(service.isRunning()).isTrue();
        }

        @Test
        void should_not_be_running_after_stop() {
            // given
            service.start();

            // when
            service.stop();

            // then
            assertThat(service.isRunning()).isFalse();
        }
    }

    @Nested
    class Tick {

        @Test
        void should_not_process_when_not_running() {
            // given
            var elevator = new Elevator(new ElevatorId(0), 9);
            elevator.addDestination(new Floor(5));

            // when
            service.tick();

            // then
            then(elevatorRepository).should(never()).findAll();
        }

        @Test
        void should_process_elevators_when_running() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            service.tick();

            // then
            then(elevatorRepository).should().findAll();
        }

        @Test
        void should_not_save_if_no_state_change() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            service.tick();

            // then
            then(elevatorRepository).should(never()).save(any());
        }

        @Test
        void should_move_elevator_towards_destination() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            elevator.addDestination(new Floor(5));
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            service.tick();

            // then
            then(elevatorRepository).should().save(elevator);
            assertThat(elevator.getCurrentFloor().number()).isEqualTo(1);
        }

        @Test
        void should_publish_state_update_on_change() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            elevator.addDestination(new Floor(5));
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            service.tick();

            // then
            then(eventPublisher).should().publishStateUpdate(any());
        }
    }

    @Nested
    class ElevatorStateTransitions {

        @Test
        void should_open_doors_when_arrived() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            elevator.addDestination(new Floor(1));
            elevator.moveUp();
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            service.tick();

            // then
            then(elevatorRepository).should().save(elevator);
        }

        @Test
        void should_process_stopped_elevator() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            elevator.addDestination(new Floor(1));
            elevator.moveUp();
            elevator.stop();
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            service.tick();

            // then
            assertThat(elevator.getState()).isEqualTo(ElevatorState.DOOR_OPENING);
        }

        @Test
        void should_complete_door_opening() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            elevator.openDoors();
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            service.tick();

            // then
            assertThat(elevator.getState()).isEqualTo(ElevatorState.DOOR_OPEN);
        }

        @Test
        void should_close_doors_after_timer() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            elevator.openDoors();
            elevator.doorsOpened();
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            for (int i = 0; i < 5; i++) {
                service.tick();
            }

            // then
            assertThat(elevator.getState()).isIn(ElevatorState.DOOR_CLOSING, ElevatorState.IDLE);
        }

        @Test
        void should_become_idle_after_doors_close_with_no_destinations() {
            // given
            service.start();
            var elevator = new Elevator(new ElevatorId(0), 9);
            elevator.openDoors();
            elevator.doorsOpened();
            elevator.closeDoors();
            given(elevatorRepository.findAll()).willReturn(List.of(elevator));

            // when
            service.tick();

            // then
            assertThat(elevator.getState()).isEqualTo(ElevatorState.IDLE);
        }
    }

    @Nested
    class MultipleElevators {

        @Test
        void should_process_all_elevators() {
            // given
            service.start();
            var elevator1 = new Elevator(new ElevatorId(0), 9);
            var elevator2 = new Elevator(new ElevatorId(1), 9);
            elevator1.addDestination(new Floor(3));
            elevator2.addDestination(new Floor(5));
            given(elevatorRepository.findAll()).willReturn(List.of(elevator1, elevator2));

            // when
            service.tick();

            // then
            assertThat(elevator1.getCurrentFloor().number()).isEqualTo(1);
            assertThat(elevator2.getCurrentFloor().number()).isEqualTo(1);
        }
    }
}
