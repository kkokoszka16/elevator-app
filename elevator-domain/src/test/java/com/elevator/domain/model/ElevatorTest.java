package com.elevator.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElevatorTest {

    private static final int MAX_FLOOR = 9;
    private Elevator elevator;

    @BeforeEach
    void setUp() {
        elevator = new Elevator(new ElevatorId(1), MAX_FLOOR);
    }

    @Nested
    class InitialState {

        @Test
        void should_start_at_ground_floor() {
            // then
            assertThat(elevator.getCurrentFloor().number()).isZero();
        }

        @Test
        void should_start_with_idle_direction() {
            // then
            assertThat(elevator.getDirection()).isEqualTo(Direction.IDLE);
        }

        @Test
        void should_start_with_closed_doors() {
            // then
            assertThat(elevator.getDoorState()).isEqualTo(DoorState.CLOSED);
        }

        @Test
        void should_start_in_idle_state() {
            // then
            assertThat(elevator.getState()).isEqualTo(ElevatorState.IDLE);
        }

        @Test
        void should_start_with_no_destinations() {
            // then
            assertThat(elevator.hasDestinations()).isFalse();
            assertThat(elevator.getDestinations()).isEmpty();
        }

        @Test
        void should_be_idle() {
            // then
            assertThat(elevator.isIdle()).isTrue();
        }
    }

    @Nested
    class DestinationManagement {

        @Test
        void should_add_destination_when_valid_floor() {
            // given
            var floor = new Floor(5);

            // when
            elevator.addDestination(floor);

            // then
            assertThat(elevator.hasDestination(floor)).isTrue();
            assertThat(elevator.hasDestinations()).isTrue();
        }

        @Test
        void should_throw_exception_when_floor_exceeds_max() {
            // given
            var floor = new Floor(MAX_FLOOR + 1);

            // when / then
            assertThatThrownBy(() -> elevator.addDestination(floor))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceeds building height");
        }

        @Test
        void should_not_add_current_floor_as_destination() {
            // given
            elevator.addDestination(new Floor(0));

            // then
            assertThat(elevator.hasDestinations()).isFalse();
        }

        @Test
        void should_remove_destination() {
            // given
            var floor = new Floor(5);
            elevator.addDestination(floor);

            // when
            elevator.removeDestination(floor);

            // then
            assertThat(elevator.hasDestination(floor)).isFalse();
        }

        @Test
        void should_return_unmodifiable_destinations() {
            // given
            elevator.addDestination(new Floor(5));

            // when
            var destinations = elevator.getDestinations();

            // then
            assertThatThrownBy(() -> destinations.add(new Floor(6)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void should_update_direction_to_up_when_destination_is_above() {
            // given
            elevator.doorsClosed();

            // when
            elevator.addDestination(new Floor(5));

            // then
            assertThat(elevator.getDirection()).isEqualTo(Direction.UP);
        }
    }

    @Nested
    class Movement {

        @Test
        void should_move_up_when_doors_closed() {
            // when
            elevator.moveUp();

            // then
            assertThat(elevator.getCurrentFloor().number()).isEqualTo(1);
            assertThat(elevator.getDirection()).isEqualTo(Direction.UP);
            assertThat(elevator.getState()).isEqualTo(ElevatorState.MOVING);
        }

        @Test
        void should_move_down_when_not_at_ground() {
            // given
            elevator.moveUp();
            elevator.moveUp();

            // when
            elevator.moveDown();

            // then
            assertThat(elevator.getCurrentFloor().number()).isEqualTo(1);
            assertThat(elevator.getDirection()).isEqualTo(Direction.DOWN);
        }

        @Test
        void should_throw_exception_when_moving_up_at_top_floor() {
            // given
            for (int i = 0; i < MAX_FLOOR; i++) {
                elevator.moveUp();
            }

            // when / then
            assertThatThrownBy(() -> elevator.moveUp())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("top floor");
        }

        @Test
        void should_throw_exception_when_moving_down_at_ground_floor() {
            // when / then
            assertThatThrownBy(() -> elevator.moveDown())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ground floor");
        }

        @Test
        void should_throw_exception_when_moving_with_doors_open() {
            // given
            elevator.openDoors();
            elevator.doorsOpened();

            // when / then
            assertThatThrownBy(() -> elevator.moveUp())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("doors");
        }

        @Test
        void should_stop_elevator() {
            // given
            elevator.moveUp();

            // when
            elevator.stop();

            // then
            assertThat(elevator.getState()).isEqualTo(ElevatorState.STOPPED);
        }
    }

    @Nested
    class DoorOperations {

        @Test
        void should_open_doors() {
            // when
            elevator.openDoors();

            // then
            assertThat(elevator.getDoorState()).isEqualTo(DoorState.OPENING);
            assertThat(elevator.getState()).isEqualTo(ElevatorState.DOOR_OPENING);
        }

        @Test
        void should_complete_door_opening() {
            // given
            elevator.openDoors();

            // when
            elevator.doorsOpened();

            // then
            assertThat(elevator.getDoorState()).isEqualTo(DoorState.OPEN);
            assertThat(elevator.getState()).isEqualTo(ElevatorState.DOOR_OPEN);
        }

        @Test
        void should_close_doors() {
            // given
            elevator.openDoors();
            elevator.doorsOpened();

            // when
            elevator.closeDoors();

            // then
            assertThat(elevator.getDoorState()).isEqualTo(DoorState.CLOSING);
            assertThat(elevator.getState()).isEqualTo(ElevatorState.DOOR_CLOSING);
        }

        @Test
        void should_complete_door_closing_and_become_idle_when_no_destinations() {
            // given
            elevator.openDoors();
            elevator.doorsOpened();
            elevator.closeDoors();

            // when
            elevator.doorsClosed();

            // then
            assertThat(elevator.getDoorState()).isEqualTo(DoorState.CLOSED);
            assertThat(elevator.getState()).isEqualTo(ElevatorState.IDLE);
            assertThat(elevator.getDirection()).isEqualTo(Direction.IDLE);
        }

        @Test
        void should_complete_door_closing_and_start_moving_when_has_destinations() {
            // given
            elevator.addDestination(new Floor(5));
            elevator.openDoors();
            elevator.doorsOpened();
            elevator.closeDoors();

            // when
            elevator.doorsClosed();

            // then
            assertThat(elevator.getDoorState()).isEqualTo(DoorState.CLOSED);
            assertThat(elevator.getState()).isEqualTo(ElevatorState.MOVING);
        }

        @Test
        void should_remove_current_floor_from_destinations_when_doors_open() {
            // given
            elevator.addDestination(new Floor(1));
            elevator.moveUp();
            elevator.openDoors();

            // when
            elevator.doorsOpened();

            // then
            assertThat(elevator.hasDestination(new Floor(1))).isFalse();
        }
    }

    @Nested
    class ServiceRequest {

        @ParameterizedTest(name = "idle elevator can service floor {0} direction {1}: should be true")
        @CsvSource({
                "5, UP",
                "5, DOWN",
                "0, UP",
                "9, DOWN"
        })
        void should_service_any_request_when_idle(int floorNum, Direction direction) {
            // given
            var floor = new Floor(floorNum);

            // when
            var result = elevator.canServiceRequest(floor, direction);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void should_service_request_in_same_direction_ahead() {
            // given
            elevator.addDestination(new Floor(5));
            elevator.moveUp();

            // when
            var result = elevator.canServiceRequest(new Floor(3), Direction.UP);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void should_not_service_request_in_opposite_direction() {
            // given
            elevator.addDestination(new Floor(5));
            elevator.moveUp();

            // when
            var result = elevator.canServiceRequest(new Floor(3), Direction.DOWN);

            // then
            assertThat(result).isFalse();
        }

        @Test
        void should_not_service_request_behind_when_moving() {
            // given
            elevator.addDestination(new Floor(5));
            elevator.moveUp();
            elevator.moveUp();

            // when
            var result = elevator.canServiceRequest(new Floor(0), Direction.UP);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class DistanceCalculation {

        @ParameterizedTest(name = "distance from ground to floor {0} should be {0}")
        @ValueSource(ints = {0, 1, 5, 9})
        void should_calculate_distance_from_ground(int targetFloor) {
            // given
            var floor = new Floor(targetFloor);

            // when
            var distance = elevator.distanceTo(floor);

            // then
            assertThat(distance).isEqualTo(targetFloor);
        }

        @Test
        void should_calculate_distance_from_non_ground() {
            // given
            elevator.moveUp();
            elevator.moveUp();
            elevator.moveUp();

            // when
            var distance = elevator.distanceTo(new Floor(7));

            // then
            assertThat(distance).isEqualTo(4);
        }
    }

    @Nested
    class NextDestination {

        @Test
        void should_return_null_when_no_destinations() {
            // when
            var next = elevator.getNextDestination();

            // then
            assertThat(next).isNull();
        }

        @Test
        void should_return_closest_destination_in_direction() {
            // given
            elevator.addDestination(new Floor(3));
            elevator.addDestination(new Floor(7));
            elevator.addDestination(new Floor(5));

            // when
            var next = elevator.getNextDestination();

            // then
            assertThat(next.number()).isEqualTo(3);
        }

        @Test
        void should_return_destination_when_going_down() {
            // given
            for (int i = 0; i < 8; i++) {
                elevator.moveUp();
            }
            elevator.addDestination(new Floor(3));
            elevator.addDestination(new Floor(5));

            // when
            var next = elevator.getNextDestination();

            // then
            assertThat(next.number()).isEqualTo(5);
        }
    }

    @Nested
    class ShouldStopAtCurrentFloor {

        @Test
        void should_return_true_when_current_floor_is_destination() {
            // given
            elevator.addDestination(new Floor(1));
            elevator.moveUp();

            // when
            var result = elevator.shouldStopAtCurrentFloor();

            // then
            assertThat(result).isTrue();
        }

        @Test
        void should_return_false_when_current_floor_is_not_destination() {
            // given
            elevator.addDestination(new Floor(5));
            elevator.moveUp();

            // when
            var result = elevator.shouldStopAtCurrentFloor();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class IsMovingTowards {

        @Test
        void should_return_false_when_idle() {
            // when
            var result = elevator.isMovingTowards(new Floor(5));

            // then
            assertThat(result).isFalse();
        }

        @Test
        void should_return_true_when_moving_up_and_floor_is_above() {
            // given
            elevator.addDestination(new Floor(5));
            elevator.moveUp();

            // when
            var result = elevator.isMovingTowards(new Floor(3));

            // then
            assertThat(result).isTrue();
        }

        @Test
        void should_return_false_when_moving_up_and_floor_is_below() {
            // given
            elevator.addDestination(new Floor(5));
            elevator.moveUp();
            elevator.moveUp();

            // when
            var result = elevator.isMovingTowards(new Floor(1));

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class SetIdle {

        @Test
        void should_set_direction_to_idle() {
            // given
            elevator.addDestination(new Floor(5));
            elevator.moveUp();

            // when
            elevator.setIdle();

            // then
            assertThat(elevator.getDirection()).isEqualTo(Direction.IDLE);
            assertThat(elevator.getState()).isEqualTo(ElevatorState.IDLE);
        }
    }
}
