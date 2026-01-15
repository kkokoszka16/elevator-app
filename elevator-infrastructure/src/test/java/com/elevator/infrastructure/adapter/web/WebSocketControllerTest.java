package com.elevator.infrastructure.adapter.web;

import com.elevator.application.port.in.CallElevatorUseCase;
import com.elevator.application.port.in.SelectFloorUseCase;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.infrastructure.adapter.web.dto.CallElevatorRequest;
import com.elevator.infrastructure.adapter.web.dto.SelectFloorRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest {

    @Mock
    private CallElevatorUseCase callElevatorUseCase;

    @Mock
    private SelectFloorUseCase selectFloorUseCase;

    private WebSocketController controller;

    @BeforeEach
    void setUp() {
        controller = new WebSocketController(callElevatorUseCase, selectFloorUseCase);
    }

    @Nested
    class CallElevator {

        @Test
        void should_call_elevator_with_correct_parameters() {
            // given
            var request = new CallElevatorRequest(5, "UP");

            // when
            controller.callElevator(request);

            // then
            var floorCaptor = ArgumentCaptor.forClass(Floor.class);
            var directionCaptor = ArgumentCaptor.forClass(Direction.class);
            then(callElevatorUseCase).should().callElevator(floorCaptor.capture(), directionCaptor.capture());

            assertThat(floorCaptor.getValue().number()).isEqualTo(5);
            assertThat(directionCaptor.getValue()).isEqualTo(Direction.UP);
        }

        @Test
        void should_handle_lowercase_direction() {
            // given
            var request = new CallElevatorRequest(3, "down");

            // when
            controller.callElevator(request);

            // then
            var directionCaptor = ArgumentCaptor.forClass(Direction.class);
            then(callElevatorUseCase).should().callElevator(new Floor(3), Direction.DOWN);
        }

        @ParameterizedTest(name = "should call elevator to floor {0} going {1}")
        @CsvSource({
                "0, UP",
                "5, UP",
                "9, DOWN",
                "3, down"
        })
        void should_handle_various_requests(int floor, String direction) {
            // given
            var request = new CallElevatorRequest(floor, direction);

            // when
            controller.callElevator(request);

            // then
            var floorCaptor = ArgumentCaptor.forClass(Floor.class);
            var directionCaptor = ArgumentCaptor.forClass(Direction.class);
            then(callElevatorUseCase).should().callElevator(floorCaptor.capture(), directionCaptor.capture());

            assertThat(floorCaptor.getValue().number()).isEqualTo(floor);
            assertThat(directionCaptor.getValue()).isEqualTo(Direction.valueOf(direction.toUpperCase()));
        }
    }

    @Nested
    class SelectFloor {

        @Test
        void should_select_floor_with_correct_parameters() {
            // given
            var elevatorId = 1;
            var request = new SelectFloorRequest(7);

            // when
            controller.selectFloor(elevatorId, request);

            // then
            var idCaptor = ArgumentCaptor.forClass(ElevatorId.class);
            var floorCaptor = ArgumentCaptor.forClass(Floor.class);
            then(selectFloorUseCase).should().selectFloor(idCaptor.capture(), floorCaptor.capture());

            assertThat(idCaptor.getValue().value()).isEqualTo(1);
            assertThat(floorCaptor.getValue().number()).isEqualTo(7);
        }

        @ParameterizedTest(name = "should select floor {1} for elevator {0}")
        @CsvSource({
                "0, 5",
                "1, 0",
                "2, 9",
                "0, 3"
        })
        void should_handle_various_selections(int elevatorId, int floor) {
            // given
            var request = new SelectFloorRequest(floor);

            // when
            controller.selectFloor(elevatorId, request);

            // then
            var idCaptor = ArgumentCaptor.forClass(ElevatorId.class);
            var floorCaptor = ArgumentCaptor.forClass(Floor.class);
            then(selectFloorUseCase).should().selectFloor(idCaptor.capture(), floorCaptor.capture());

            assertThat(idCaptor.getValue().value()).isEqualTo(elevatorId);
            assertThat(floorCaptor.getValue().number()).isEqualTo(floor);
        }
    }
}
