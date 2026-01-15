package com.elevator.infrastructure.integration;

import com.elevator.application.port.in.CallElevatorUseCase;
import com.elevator.application.port.in.GetSystemStatusUseCase;
import com.elevator.application.port.in.SelectFloorUseCase;
import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.exception.NoAvailableElevatorException;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.DoorState;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.ElevatorState;
import com.elevator.domain.model.Floor;
import com.elevator.infrastructure.adapter.web.ElevatorController;
import com.elevator.infrastructure.adapter.web.GlobalExceptionHandler;
import com.elevator.infrastructure.adapter.web.dto.ElevatorMapperImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
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
@Import({GlobalExceptionHandler.class, ElevatorMapperImpl.class})
@DisplayName("ElevatorController Integration Tests")
class ElevatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetSystemStatusUseCase getSystemStatusUseCase;

    @MockBean
    private CallElevatorUseCase callElevatorUseCase;

    @MockBean
    private SelectFloorUseCase selectFloorUseCase;

    @Test
    @DisplayName("should return all elevators status with 200")
    void should_return_all_elevators_status_with_200() throws Exception {
        var config = new BuildingConfig(10, 2, Duration.ofSeconds(3), Duration.ofSeconds(2));
        var elevator1 = createElevator(1, 5);
        var elevator2 = createElevator(2, 8);

        given(getSystemStatusUseCase.getConfig()).willReturn(config);
        given(getSystemStatusUseCase.getAllElevators()).willReturn(List.of(elevator1, elevator2));

        mockMvc.perform(get("/api/v1/elevators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfFloors").value(10))
                .andExpect(jsonPath("$.numberOfElevators").value(2))
                .andExpect(jsonPath("$.elevators").isArray())
                .andExpect(jsonPath("$.elevators.length()").value(2))
                .andExpect(jsonPath("$.elevators[0].id").value(1))
                .andExpect(jsonPath("$.elevators[0].currentFloor").value(5))
                .andExpect(jsonPath("$.elevators[1].id").value(2))
                .andExpect(jsonPath("$.elevators[1].currentFloor").value(8));
    }

    @Test
    @DisplayName("should return single elevator status with 200")
    void should_return_single_elevator_status_with_200() throws Exception {
        var elevator = createElevator(1, 7);

        given(getSystemStatusUseCase.getElevator(new ElevatorId(1)))
                .willReturn(Optional.of(elevator));

        mockMvc.perform(get("/api/v1/elevators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.currentFloor").value(7))
                .andExpect(jsonPath("$.state").value("IDLE"))
                .andExpect(jsonPath("$.direction").value("IDLE"))
                .andExpect(jsonPath("$.doorState").value("CLOSED"));
    }

    @Test
    @DisplayName("should return 404 when elevator not found")
    void should_return_404_when_elevator_not_found() throws Exception {
        given(getSystemStatusUseCase.getElevator(new ElevatorId(999)))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/elevators/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ELEVATOR_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("should accept call elevator request with 202")
    void should_accept_call_elevator_request_with_202() throws Exception {
        var request = """
                {
                    "floor": 5,
                    "direction": "UP"
                }
                """;

        mockMvc.perform(post("/api/v1/elevators/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isAccepted());

        then(callElevatorUseCase).should().callElevator(new Floor(5), Direction.UP);
    }

    @ParameterizedTest(name = "{index}: floor={0}, direction={1}")
    @CsvSource({
            "1, UP",
            "10, DOWN",
            "5, UP",
            "7, DOWN"
    })
    @DisplayName("should handle various call elevator requests")
    void should_handle_various_call_elevator_requests(int floor, String direction) throws Exception {
        var request = String.format("""
                {
                    "floor": %d,
                    "direction": "%s"
                }
                """, floor, direction);

        mockMvc.perform(post("/api/v1/elevators/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("should return 400 for invalid floor in call request")
    void should_return_400_for_invalid_floor_in_call_request() throws Exception {
        var request = """
                {
                    "floor": 15,
                    "direction": "UP"
                }
                """;

        willThrow(new InvalidFloorException(15))
                .given(callElevatorUseCase)
                .callElevator(any(Floor.class), any(Direction.class));

        mockMvc.perform(post("/api/v1/elevators/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FLOOR"));
    }

    @Test
    @DisplayName("should return 503 when no elevator available")
    void should_return_503_when_no_elevator_available() throws Exception {
        var request = """
                {
                    "floor": 5,
                    "direction": "UP"
                }
                """;

        willThrow(new NoAvailableElevatorException(new Floor(5)))
                .given(callElevatorUseCase)
                .callElevator(any(Floor.class), any(Direction.class));

        mockMvc.perform(post("/api/v1/elevators/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("NO_AVAILABLE_ELEVATOR"));
    }

    @Test
    @DisplayName("should accept select floor request with 202")
    void should_accept_select_floor_request_with_202() throws Exception {
        var request = """
                {
                    "floor": 8
                }
                """;

        mockMvc.perform(post("/api/v1/elevators/1/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isAccepted());

        then(selectFloorUseCase).should()
                .selectFloor(new ElevatorId(1), new Floor(8));
    }

    @ParameterizedTest(name = "{index}: elevatorId={0}, floor={1}")
    @CsvSource({
            "1, 5",
            "2, 10",
            "3, 1"
    })
    @DisplayName("should handle various select floor requests")
    void should_handle_various_select_floor_requests(int elevatorId, int floor) throws Exception {
        var request = String.format("""
                {
                    "floor": %d
                }
                """, floor);

        mockMvc.perform(post("/api/v1/elevators/" + elevatorId + "/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("should return 404 when selecting floor for non-existent elevator")
    void should_return_404_when_selecting_floor_for_non_existent_elevator() throws Exception {
        var request = """
                {
                    "floor": 5
                }
                """;

        willThrow(new ElevatorNotFoundException(new ElevatorId(999)))
                .given(selectFloorUseCase)
                .selectFloor(any(ElevatorId.class), any(Floor.class));

        mockMvc.perform(post("/api/v1/elevators/999/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ELEVATOR_NOT_FOUND"));
    }

    @Test
    @DisplayName("should return 400 for missing required fields")
    void should_return_400_for_missing_required_fields() throws Exception {
        var request = """
                {
                    "direction": "UP"
                }
                """;

        mockMvc.perform(post("/api/v1/elevators/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 for invalid direction value")
    void should_return_400_for_invalid_direction_value() throws Exception {
        var request = """
                {
                    "floor": 5,
                    "direction": "SIDEWAYS"
                }
                """;

        mockMvc.perform(post("/api/v1/elevators/call")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return elevator with destinations")
    void should_return_elevator_with_destinations() throws Exception {
        var elevator = createElevator(1, 5);
        elevator.addDestination(new Floor(8));
        elevator.addDestination(new Floor(10));

        given(getSystemStatusUseCase.getElevator(new ElevatorId(1)))
                .willReturn(Optional.of(elevator));

        mockMvc.perform(get("/api/v1/elevators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destinations").isArray())
                .andExpect(jsonPath("$.destinations.length()").value(2));
    }

    private Elevator createElevator(int id, int currentFloor) {
        var elevator = new Elevator(new ElevatorId(id), 10);
        for (int i = 0; i < currentFloor; i++) {
            elevator.moveUp();
        }
        if (currentFloor > 0) {
            elevator.stop();
            elevator.closeDoors();
            elevator.doorsClosed();
        }
        return elevator;
    }
}
