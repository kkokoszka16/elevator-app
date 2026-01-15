package com.elevator.infrastructure.adapter.web;

import com.elevator.application.port.in.ConfigureSystemUseCase;
import com.elevator.application.port.in.ElevatorSimulationUseCase;
import com.elevator.application.port.in.GetSystemStatusUseCase;
import com.elevator.infrastructure.adapter.web.dto.BuildingConfigDto;
import com.elevator.infrastructure.adapter.web.dto.ElevatorMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Tag(name = "System", description = "System configuration operations")
public class SystemController {

    private final GetSystemStatusUseCase getSystemStatusUseCase;
    private final ConfigureSystemUseCase configureSystemUseCase;
    private final ElevatorSimulationUseCase simulationUseCase;
    private final ElevatorMapper elevatorMapper;

    @GetMapping("/config")
    @Operation(summary = "Get building configuration", description = "Returns the current building configuration")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved configuration")
    public BuildingConfigDto getConfig() {
        var config = getSystemStatusUseCase.getConfig();
        return elevatorMapper.toConfigDto(config);
    }

    @PutMapping("/config")
    @Operation(summary = "Update building configuration", description = "Updates the building configuration and reinitializes the system")
    @ApiResponse(responseCode = "200", description = "Configuration updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid configuration")
    public BuildingConfigDto updateConfig(@Valid @RequestBody BuildingConfigDto request) {
        var config = elevatorMapper.toConfig(request);
        configureSystemUseCase.configure(config);
        return elevatorMapper.toConfigDto(config);
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reset system", description = "Resets the elevator system to initial state")
    @ApiResponse(responseCode = "204", description = "System reset successfully")
    public void reset() {
        configureSystemUseCase.reset();
    }

    @PostMapping("/simulation/start")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Start simulation", description = "Starts the elevator simulation")
    @ApiResponse(responseCode = "200", description = "Simulation started")
    public Map<String, Boolean> startSimulation() {
        simulationUseCase.start();
        return Map.of("running", true);
    }

    @PostMapping("/simulation/stop")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Stop simulation", description = "Stops the elevator simulation")
    @ApiResponse(responseCode = "200", description = "Simulation stopped")
    public Map<String, Boolean> stopSimulation() {
        simulationUseCase.stop();
        return Map.of("running", false);
    }

    @GetMapping("/simulation/status")
    @Operation(summary = "Get simulation status", description = "Returns whether the simulation is running")
    @ApiResponse(responseCode = "200", description = "Status retrieved")
    public Map<String, Boolean> getSimulationStatus() {
        return Map.of("running", simulationUseCase.isRunning());
    }
}
