import { useCallback, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Elevator, WebSocketMessage } from '../types/elevator';

interface UseWebSocketOptions {
  onElevatorUpdate: (elevators: Elevator[]) => void;
}

export function useWebSocket({ onElevatorUpdate }: UseWebSocketOptions) {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const wsUrl = `${window.location.protocol === 'https:' ? 'https:' : 'http:'}//${window.location.host}/ws`;

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        setConnected(true);
        console.log('WebSocket connected');

        client.subscribe('/topic/elevators', (message) => {
          const data: WebSocketMessage<Elevator[]> = JSON.parse(message.body);
          onElevatorUpdate(data.payload);
        });

        client.subscribe('/topic/events', (message) => {
          console.log('Event received:', JSON.parse(message.body));
        });
      },
      onDisconnect: () => {
        setConnected(false);
        console.log('WebSocket disconnected');
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [onElevatorUpdate]);

  const callElevator = useCallback((floor: number, direction: 'UP' | 'DOWN') => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: '/app/call',
        body: JSON.stringify({ floor, direction }),
      });
    }
  }, []);

  const selectFloor = useCallback((elevatorId: number, floor: number) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: `/app/select/${elevatorId}`,
        body: JSON.stringify({ floor }),
      });
    }
  }, []);

  return { connected, callElevator, selectFloor };
}
