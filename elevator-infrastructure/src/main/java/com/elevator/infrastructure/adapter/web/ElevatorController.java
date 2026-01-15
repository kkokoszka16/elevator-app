package com.elevator.infrastructure.adapter.web;

import com.elevator.application.port.in.CallElevatorUseCase;
import com.elevator.application.port.in.GetSystemStatusUseCase;
import com.elevator.application.port.in.SelectFloorUseCase;
import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.infrastructure.adapter.web.dto.CallElevatorRequest;
import com.elevator.infrastructure.adapter.web.dto.ElevatorDto;
import com.elevator.infrastructure.adapter.web.dto.ElevatorMapper;
import com.elevator.infrastructure.adapter.web.dto.ElevatorSystemStatusDto;
import com.elevator.infrastructure.adapter.web.dto.SelectFloorRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/elevators")
@RequiredArgsConstructor
@Tag(name = "Elevator", description = "Elevator management operations")
public class ElevatorController {

    private final GetSystemStatusUseCase getSystemStatusUseCase;
    private final CallElevatorUseCase callElevatorUseCase;
    private final SelectFloorUseCase selectFloorUseCase;
    private final ElevatorMapper elevatorMapper;

    @GetMapping
    @Operation(summary = "Get all elevators status", description = "Returns the current status of all elevators in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved elevator status")
    public ElevatorSystemStatusDto getAllElevators() {
        var config = getSystemStatusUseCase.getConfig();
        var elevators = getSystemStatusUseCase.getAllElevators();
        return elevatorMapper.toSystemStatusDto(config, elevators);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single elevator status", description = "Returns the current status of a specific elevator")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved elevator status")
    @ApiResponse(responseCode = "404", description = "Elevator not found")
    public ElevatorDto getElevator(@PathVariable int id) {
        var elevator = getSystemStatusUseCase.getElevator(new ElevatorId(id))
                .orElseThrow(() -> new ElevatorNotFoundException(new ElevatorId(id)));
        return elevatorMapper.toDto(elevator);
    }

    @PostMapping("/call")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Call an elevator", description = "Request an elevator to arrive at a specific floor")
    @ApiResponse(responseCode = "202", description = "Elevator call request accepted")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public void callElevator(@Valid @RequestBody CallElevatorRequest request) {
        var floor = elevatorMapper.toFloor(request.floor());
        var direction = elevatorMapper.toDirection(request.direction());
        callElevatorUseCase.callElevator(floor, direction);
    }

    @PostMapping("/{id}/select")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Select floor from inside elevator", description = "Select a destination floor from inside an elevator")
    @ApiResponse(responseCode = "202", description = "Floor selection accepted")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Elevator not found")
    public void selectFloor(@PathVariable int id, @Valid @RequestBody SelectFloorRequest request) {
        var elevatorId = new ElevatorId(id);
        var floor = new Floor(request.floor());
        selectFloorUseCase.selectFloor(elevatorId, floor);
    }
}
