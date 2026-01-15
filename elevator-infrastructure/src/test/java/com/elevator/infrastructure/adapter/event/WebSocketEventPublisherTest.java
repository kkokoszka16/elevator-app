package com.elevator.infrastructure.adapter.event;

import com.elevator.domain.event.DoorStateChangedEvent;
import com.elevator.domain.event.ElevatorArrivedEvent;
import com.elevator.domain.event.ElevatorMovedEvent;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.DoorState;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.infrastructure.adapter.web.dto.ElevatorDto;
import com.elevator.infrastructure.adapter.web.dto.ElevatorMapper;
import com.elevator.infrastructure.adapter.web.dto.WebSocketMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WebSocketEventPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ElevatorMapper elevatorMapper;

    private WebSocketEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new WebSocketEventPublisher(messagingTemplate, elevatorMapper);
    }

    @Nested
    class PublishEvent {

        @Test
        void should_publish_elevator_moved_event_to_events_topic() {
            // given
            var event = new ElevatorMovedEvent(
                    new ElevatorId(0),
                    new Floor(3),
                    new Floor(4),
                    Direction.UP
            );

            // when
            publisher.publish(event);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<WebSocketMessage<Object>> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/events"), captor.capture());

            var message = captor.getValue();
            assertThat(message.type()).isEqualTo("ELEVATOR_MOVED");
        }

        @Test
        void should_publish_elevator_arrived_event_to_events_topic() {
            // given
            var event = new ElevatorArrivedEvent(new ElevatorId(1), new Floor(5));

            // when
            publisher.publish(event);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<WebSocketMessage<Object>> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/events"), captor.capture());

            var message = captor.getValue();
            assertThat(message.type()).isEqualTo("ELEVATOR_ARRIVED");
        }

        @Test
        void should_publish_door_state_changed_event_to_events_topic() {
            // given
            var event = new DoorStateChangedEvent(
                    new ElevatorId(0),
                    DoorState.CLOSED,
                    DoorState.OPENING
            );

            // when
            publisher.publish(event);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<WebSocketMessage<Object>> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/events"), captor.capture());

            var message = captor.getValue();
            assertThat(message.type()).isEqualTo("DOOR_STATE_CHANGED");
        }
    }

    @Nested
    class PublishStateUpdate {

        @Test
        void should_publish_state_update_to_elevators_topic() {
            // given
            var elevator = new Elevator(new ElevatorId(0), 9);
            var elevators = List.of(elevator);
            var dto = new ElevatorDto(0, 0, "IDLE", "CLOSED", "IDLE", List.of());
            given(elevatorMapper.toDtoList(elevators)).willReturn(List.of(dto));

            // when
            publisher.publishStateUpdate(elevators);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<WebSocketMessage<Object>> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/elevators"), captor.capture());

            var message = captor.getValue();
            assertThat(message.type()).isEqualTo("ELEVATOR_STATE_UPDATE");
        }

        @Test
        void should_include_all_elevators_in_payload() {
            // given
            var elevator1 = new Elevator(new ElevatorId(0), 9);
            var elevator2 = new Elevator(new ElevatorId(1), 9);
            var elevators = List.of(elevator1, elevator2);
            var dto1 = new ElevatorDto(0, 0, "IDLE", "CLOSED", "IDLE", List.of());
            var dto2 = new ElevatorDto(1, 0, "IDLE", "CLOSED", "IDLE", List.of());
            given(elevatorMapper.toDtoList(elevators)).willReturn(List.of(dto1, dto2));

            // when
            publisher.publishStateUpdate(elevators);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<WebSocketMessage<Object>> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/elevators"), captor.capture());

            var message = captor.getValue();
            assertThat(message.payload()).isInstanceOf(List.class);
            var payload = (List<?>) message.payload();
            assertThat(payload).hasSize(2);
        }

        @Test
        void should_handle_empty_elevator_list() {
            // given
            List<Elevator> elevators = List.of();
            given(elevatorMapper.toDtoList(elevators)).willReturn(List.of());

            // when
            publisher.publishStateUpdate(elevators);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<WebSocketMessage<Object>> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/elevators"), captor.capture());

            var message = captor.getValue();
            var payload = (List<?>) message.payload();
            assertThat(payload).isEmpty();
        }
    }
}
