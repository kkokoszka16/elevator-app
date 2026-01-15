package com.elevator.domain.service;

import com.elevator.domain.model.Direction;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LookSchedulingStrategyTest {

    private static final int MAX_FLOOR = 9;
    private LookSchedulingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LookSchedulingStrategy();
    }

    @Nested
    class EmptyElevatorList {

        @Test
        void should_return_empty_when_no_elevators() {
            // given
            List<Elevator> elevators = Collections.emptyList();

            // when
            var result = strategy.selectElevator(new Floor(5), Direction.UP, elevators);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class SingleElevator {

        @Test
        void should_select_single_idle_elevator() {
            // given
            var elevator = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevators = List.of(elevator);

            // when
            var result = strategy.selectElevator(new Floor(5), Direction.UP, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isZero();
        }

        @Test
        void should_select_single_moving_elevator() {
            // given
            var elevator = new Elevator(new ElevatorId(0), MAX_FLOOR);
            elevator.addDestination(new Floor(7));
            elevator.moveUp();
            var elevators = List.of(elevator);

            // when
            var result = strategy.selectElevator(new Floor(5), Direction.UP, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isZero();
        }
    }

    @Nested
    class MultipleIdleElevators {

        @Test
        void should_select_closest_idle_elevator() {
            // given
            var elevator0 = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevator1 = new Elevator(new ElevatorId(1), MAX_FLOOR);
            for (int i = 0; i < 5; i++) {
                elevator1.moveUp();
            }
            elevator1.setIdle();

            var elevators = List.of(elevator0, elevator1);

            // when
            var result = strategy.selectElevator(new Floor(4), Direction.UP, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isEqualTo(1);
        }

        @Test
        void should_select_closest_when_request_at_ground() {
            // given
            var elevator0 = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevator1 = new Elevator(new ElevatorId(1), MAX_FLOOR);
            for (int i = 0; i < 5; i++) {
                elevator1.moveUp();
            }
            elevator1.setIdle();

            var elevators = List.of(elevator0, elevator1);

            // when
            var result = strategy.selectElevator(new Floor(0), Direction.UP, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isZero();
        }
    }

    @Nested
    class ElevatorOnTheWay {

        @Test
        void should_select_elevator_going_same_direction() {
            // given
            var elevator0 = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevator1 = new Elevator(new ElevatorId(1), MAX_FLOOR);

            elevator0.addDestination(new Floor(8));
            elevator0.moveUp();

            for (int i = 0; i < 3; i++) {
                elevator1.moveUp();
            }
            elevator1.setIdle();

            var elevators = List.of(elevator0, elevator1);

            // when
            var result = strategy.selectElevator(new Floor(5), Direction.UP, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isZero();
        }

        @Test
        void should_not_select_elevator_going_opposite_direction() {
            // given
            var elevator0 = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevator1 = new Elevator(new ElevatorId(1), MAX_FLOOR);

            elevator0.addDestination(new Floor(8));
            elevator0.moveUp();

            var elevators = List.of(elevator0, elevator1);

            // when
            var result = strategy.selectElevator(new Floor(5), Direction.DOWN, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isEqualTo(1);
        }

        @ParameterizedTest(name = "elevator at floor {0} going {1}, request at floor {2} going {3} -> select elevator: {4}")
        @CsvSource({
                "2, UP, 5, UP, true",
                "7, DOWN, 3, DOWN, true",
                "2, UP, 5, DOWN, false",
                "7, DOWN, 3, UP, false"
        })
        void should_correctly_evaluate_on_the_way(int elevatorFloor, Direction elevatorDir,
                                                   int requestFloor, Direction requestDir,
                                                   boolean shouldSelect) {
            // given
            var elevator0 = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevator1 = new Elevator(new ElevatorId(1), MAX_FLOOR);

            for (int i = 0; i < elevatorFloor; i++) {
                elevator0.moveUp();
            }
            if (elevatorDir == Direction.UP) {
                elevator0.addDestination(new Floor(MAX_FLOOR));
            } else {
                elevator0.addDestination(new Floor(0));
            }

            var elevators = List.of(elevator0, elevator1);

            // when
            var result = strategy.selectElevator(new Floor(requestFloor), requestDir, elevators);

            // then
            assertThat(result).isPresent();
            if (shouldSelect) {
                assertThat(result.get().value()).isZero();
            } else {
                assertThat(result.get().value()).isEqualTo(1);
            }
        }
    }

    @Nested
    class LeastBusyElevator {

        @Test
        void should_select_elevator_with_fewer_destinations_when_all_busy() {
            // given
            var elevator0 = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevator1 = new Elevator(new ElevatorId(1), MAX_FLOOR);

            elevator0.addDestination(new Floor(2));
            elevator0.addDestination(new Floor(4));
            elevator0.addDestination(new Floor(6));

            elevator1.addDestination(new Floor(3));

            for (int i = 0; i < 8; i++) {
                elevator0.moveUp();
            }
            for (int i = 0; i < 7; i++) {
                elevator1.moveUp();
            }

            var elevators = List.of(elevator0, elevator1);

            // when
            var result = strategy.selectElevator(new Floor(2), Direction.DOWN, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isEqualTo(1);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void should_handle_request_at_same_floor_as_idle_elevator() {
            // given
            var elevator = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevators = List.of(elevator);

            // when
            var result = strategy.selectElevator(new Floor(0), Direction.UP, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isZero();
        }

        @Test
        void should_handle_request_at_max_floor() {
            // given
            var elevator = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevators = List.of(elevator);

            // when
            var result = strategy.selectElevator(new Floor(MAX_FLOOR), Direction.DOWN, elevators);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().value()).isZero();
        }

        @Test
        void should_handle_multiple_elevators_at_same_distance() {
            // given
            var elevator0 = new Elevator(new ElevatorId(0), MAX_FLOOR);
            var elevator1 = new Elevator(new ElevatorId(1), MAX_FLOOR);
            var elevators = List.of(elevator0, elevator1);

            // when
            var result = strategy.selectElevator(new Floor(5), Direction.UP, elevators);

            // then
            assertThat(result).isPresent();
        }
    }
}
