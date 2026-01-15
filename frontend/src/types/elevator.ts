export interface Elevator {
  id: number;
  currentFloor: number;
  direction: 'UP' | 'DOWN' | 'IDLE';
  doorState: 'OPEN' | 'CLOSED' | 'OPENING' | 'CLOSING';
  state: 'IDLE' | 'MOVING' | 'STOPPED' | 'DOOR_OPENING' | 'DOOR_OPEN' | 'DOOR_CLOSING';
  destinations: number[];
}

export interface BuildingConfig {
  numberOfFloors: number;
  numberOfElevators: number;
  doorOpenDurationSeconds?: number;
  floorTravelDurationSeconds?: number;
}

export interface ElevatorSystemStatus {
  numberOfFloors: number;
  numberOfElevators: number;
  elevators: Elevator[];
}

export interface WebSocketMessage<T> {
  type: string;
  timestamp: string;
  payload: T;
}

export type Direction = 'UP' | 'DOWN';

export interface CallElevatorRequest {
  floor: number;
  direction: Direction;
}

export interface SelectFloorRequest {
  floor: number;
}
