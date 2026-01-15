package com.elevator.infrastructure.adapter.event;

import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.domain.event.ElevatorEvent;
import com.elevator.domain.model.Elevator;
import com.elevator.infrastructure.adapter.web.dto.ElevatorMapper;
import com.elevator.infrastructure.adapter.web.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher implements ElevatorEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ElevatorMapper elevatorMapper;

    @Override
    public void publish(ElevatorEvent event) {
        var message = WebSocketMessage.of(event.eventType(), event);
        messagingTemplate.convertAndSend("/topic/events", message);
        log.debug("Published event: {}", event.eventType());
    }

    @Override
    public void publishStateUpdate(List<Elevator> elevators) {
        var dtos = elevatorMapper.toDtoList(elevators);
        var message = WebSocketMessage.of("ELEVATOR_STATE_UPDATE", dtos);
        messagingTemplate.convertAndSend("/topic/elevators", message);
        log.debug("Published state update for {} elevators", elevators.size());
    }
}
