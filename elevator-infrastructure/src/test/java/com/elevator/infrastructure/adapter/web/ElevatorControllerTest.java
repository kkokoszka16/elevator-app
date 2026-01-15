package com.elevator.infrastructure.adapter.web;

import com.elevator.application.port.in.CallElevatorUseCase;
import com.elevator.application.port.in.GetSystemStatusUseCase;
import com.elevator.application.port.in.SelectFloorUseCase;
import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.exception.NoAvailableElevatorException;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.infrastructure.adapter.web.dto.ElevatorMapper;
import com.elevator.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ElevatorController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class},
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.elevator.infrastructure.adapter.web.RateLimitFilter.class))
@Import({GlobalExceptionHandler.class})
class ElevatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetSystemStatusUseCase getSystemStatusUseCase;

    @MockBean
    private CallElevatorUseCase callElevatorUseCase;

    @MockBean
    private SelectFloorUseCase selectFloorUseCase;

    @MockBean
    private ElevatorMapper elevatorMapper;

    private static final BuildingConfig DEFAULT_CONFIG = new BuildingConfig(
            10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2)
    );

    @Nested
    class GetAllElevators {

        @Test
        void should_return_all_elevators() throws Exception {
            // given
            var elevator = new Elevator(new ElevatorId(0), 9);
            given(getSystemStatusUseCase.getConfig()).willReturn(DEFAULT_CONFIG);
            given(getSystemStatusUseCase.getAllElevators()).willReturn(List.of(elevator));

            // when / then
            mockMvc.perform(get("/api/v1/elevators"))
                    .andExpect(status().isOk());

            then(getSystemStatusUseCase).should().getAllElevators();
        }
    }

    @Nested
    class GetElevator {

        @Test
        void should_return_elevator_when_exists() throws Exception {
            // given
            var elevatorId = new ElevatorId(0);
            var elevator = new Elevator(elevatorId, 9);
            given(getSystemStatusUseCase.getElevator(elevatorId)).willReturn(Optional.of(elevator));

            // when / then
            mockMvc.perform(get("/api/v1/elevators/0"))
                    .andExpect(status().isOk());
        }

        @Test
        void should_return_404_when_elevator_not_found() throws Exception {
            // given
            var elevatorId = new ElevatorId(99);
            given(getSystemStatusUseCase.getElevator(elevatorId)).willReturn(Optional.empty());

            // when / then
            mockMvc.perform(get("/api/v1/elevators/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class CallElevator {

        @Test
        void should_accept_valid_call_request() throws Exception {
            // given
            given(elevatorMapper.toFloor(5)).willReturn(new Floor(5));
            given(elevatorMapper.toDirection("UP")).willReturn(Direction.UP);

            // when / then
            mockMvc.perform(post("/api/v1/elevators/call")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": 5,
                                        "direction": "UP"
                                    }
                                    """))
                    .andExpect(status().isAccepted());

            then(callElevatorUseCase).should().callElevator(any(Floor.class), any(Direction.class));
        }

        @Test
        void should_return_400_when_floor_is_null() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/elevators/call")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "direction": "UP"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_direction_is_null() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/elevators/call")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": 5
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_floor_is_negative() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/elevators/call")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": -1,
                                        "direction": "UP"
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_invalid_floor() throws Exception {
            // given
            given(elevatorMapper.toFloor(99)).willReturn(new Floor(99));
            given(elevatorMapper.toDirection("UP")).willReturn(Direction.UP);
            willThrow(new InvalidFloorException(99))
                    .given(callElevatorUseCase).callElevator(any(Floor.class), any(Direction.class));

            // when / then
            mockMvc.perform(post("/api/v1/elevators/call")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": 99,
                                        "direction": "UP"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_FLOOR"));
        }

        @Test
        void should_return_503_when_no_elevator_available() throws Exception {
            // given
            given(elevatorMapper.toFloor(5)).willReturn(new Floor(5));
            given(elevatorMapper.toDirection("UP")).willReturn(Direction.UP);
            willThrow(new NoAvailableElevatorException(new Floor(5)))
                    .given(callElevatorUseCase).callElevator(any(Floor.class), any(Direction.class));

            // when / then
            mockMvc.perform(post("/api/v1/elevators/call")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": 5,
                                        "direction": "UP"
                                    }
                                    """))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.code").value("NO_AVAILABLE_ELEVATOR"));
        }

        @ParameterizedTest(name = "should accept call to floor {0} going {1}")
        @CsvSource({
                "0, UP",
                "5, UP",
                "9, DOWN",
                "3, DOWN"
        })
        void should_accept_various_valid_calls(int floor, String direction) throws Exception {
            // given
            given(elevatorMapper.toFloor(floor)).willReturn(new Floor(floor));
            given(elevatorMapper.toDirection(direction)).willReturn(Direction.valueOf(direction));

            // when / then
            mockMvc.perform(post("/api/v1/elevators/call")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "floor": %d,
                                        "direction": "%s"
                                    }
                                    """, floor, direction)))
                    .andExpect(status().isAccepted());
        }
    }

    @Nested
    class SelectFloor {

        @Test
        void should_accept_valid_select_request() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/elevators/0/select")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": 5
                                    }
                                    """))
                    .andExpect(status().isAccepted());

            then(selectFloorUseCase).should().selectFloor(eq(new ElevatorId(0)), eq(new Floor(5)));
        }

        @Test
        void should_return_400_when_floor_is_null() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/elevators/0/select")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_floor_is_negative() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/elevators/0/select")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": -1
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_elevator_not_found() throws Exception {
            // given
            willThrow(new ElevatorNotFoundException(new ElevatorId(99)))
                    .given(selectFloorUseCase).selectFloor(any(ElevatorId.class), any(Floor.class));

            // when / then
            mockMvc.perform(post("/api/v1/elevators/99/select")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": 5
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ELEVATOR_NOT_FOUND"));
        }

        @Test
        void should_return_400_when_invalid_floor() throws Exception {
            // given
            willThrow(new InvalidFloorException(99))
                    .given(selectFloorUseCase).selectFloor(any(ElevatorId.class), any(Floor.class));

            // when / then
            mockMvc.perform(post("/api/v1/elevators/0/select")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "floor": 99
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_FLOOR"));
        }
    }
}
