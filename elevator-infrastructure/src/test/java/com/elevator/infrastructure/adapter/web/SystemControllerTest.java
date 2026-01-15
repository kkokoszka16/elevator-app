package com.elevator.infrastructure.adapter.web;

import com.elevator.application.port.in.ConfigureSystemUseCase;
import com.elevator.application.port.in.ElevatorSimulationUseCase;
import com.elevator.application.port.in.GetSystemStatusUseCase;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.infrastructure.adapter.web.dto.BuildingConfigDto;
import com.elevator.infrastructure.adapter.web.dto.ElevatorMapper;
import com.elevator.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SystemController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class},
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.elevator.infrastructure.adapter.web.RateLimitFilter.class))
@Import({GlobalExceptionHandler.class})
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetSystemStatusUseCase getSystemStatusUseCase;

    @MockBean
    private ConfigureSystemUseCase configureSystemUseCase;

    @MockBean
    private ElevatorSimulationUseCase simulationUseCase;

    @MockBean
    private ElevatorMapper elevatorMapper;

    private static final BuildingConfig DEFAULT_CONFIG = new BuildingConfig(
            10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2)
    );

    @Nested
    class GetConfig {

        @Test
        void should_return_config() throws Exception {
            // given
            given(getSystemStatusUseCase.getConfig()).willReturn(DEFAULT_CONFIG);
            given(elevatorMapper.toConfigDto(DEFAULT_CONFIG))
                    .willReturn(new BuildingConfigDto(10, 3, 3, 2));

            // when / then
            mockMvc.perform(get("/api/v1/system/config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.numberOfFloors").value(10))
                    .andExpect(jsonPath("$.numberOfElevators").value(3));
        }
    }

    @Nested
    class UpdateConfig {

        @Test
        void should_update_config() throws Exception {
            // given
            var newConfig = new BuildingConfig(15, 5, Duration.ofSeconds(4), Duration.ofSeconds(3));
            given(elevatorMapper.toConfig(any(BuildingConfigDto.class))).willReturn(newConfig);
            given(elevatorMapper.toConfigDto(newConfig))
                    .willReturn(new BuildingConfigDto(15, 5, 4, 3));

            // when / then
            mockMvc.perform(put("/api/v1/system/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "numberOfFloors": 15,
                                        "numberOfElevators": 5,
                                        "doorOpenDurationSeconds": 4,
                                        "floorTravelDurationSeconds": 3
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.numberOfFloors").value(15))
                    .andExpect(jsonPath("$.numberOfElevators").value(5));

            then(configureSystemUseCase).should().configure(newConfig);
        }

        @Test
        void should_return_400_when_invalid_config() throws Exception {
            // when / then
            mockMvc.perform(put("/api/v1/system/config")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "numberOfFloors": 0,
                                        "numberOfElevators": 3
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Reset {

        @Test
        void should_reset_system() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/system/reset"))
                    .andExpect(status().isNoContent());

            then(configureSystemUseCase).should().reset();
        }
    }

    @Nested
    class SimulationControl {

        @Test
        void should_start_simulation() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/system/simulation/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.running").value(true));

            then(simulationUseCase).should().start();
        }

        @Test
        void should_stop_simulation() throws Exception {
            // when / then
            mockMvc.perform(post("/api/v1/system/simulation/stop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.running").value(false));

            then(simulationUseCase).should().stop();
        }

        @Test
        void should_return_simulation_status_running() throws Exception {
            // given
            given(simulationUseCase.isRunning()).willReturn(true);

            // when / then
            mockMvc.perform(get("/api/v1/system/simulation/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.running").value(true));
        }

        @Test
        void should_return_simulation_status_stopped() throws Exception {
            // given
            given(simulationUseCase.isRunning()).willReturn(false);

            // when / then
            mockMvc.perform(get("/api/v1/system/simulation/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.running").value(false));
        }
    }
}
