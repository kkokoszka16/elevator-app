package com.elevator.infrastructure.adapter.web;

import com.elevator.application.port.in.CallElevatorUseCase;
import com.elevator.application.port.in.SelectFloorUseCase;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.infrastructure.adapter.web.dto.CallElevatorRequest;
import com.elevator.infrastructure.adapter.web.dto.SelectFloorRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final CallElevatorUseCase callElevatorUseCase;
    private final SelectFloorUseCase selectFloorUseCase;

    @MessageMapping("/call")
    public void callElevator(CallElevatorRequest request) {
        log.debug("WebSocket call elevator request: floor={}, direction={}", request.floor(), request.direction());
        var floor = new Floor(request.floor());
        var direction = Direction.valueOf(request.direction().toUpperCase());
        callElevatorUseCase.callElevator(floor, direction);
    }

    @MessageMapping("/select/{elevatorId}")
    public void selectFloor(@DestinationVariable int elevatorId, SelectFloorRequest request) {
        log.debug("WebSocket select floor request: elevator={}, floor={}", elevatorId, request.floor());
        var id = new ElevatorId(elevatorId);
        var floor = new Floor(request.floor());
        selectFloorUseCase.selectFloor(id, floor);
    }
}
