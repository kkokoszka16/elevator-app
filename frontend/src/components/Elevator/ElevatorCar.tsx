import React from 'react';
import { Elevator } from '../../types/elevator';
import './ElevatorCar.css';

interface ElevatorCarProps {
  elevator: Elevator;
  onClick: () => void;
}

export const ElevatorCar: React.FC<ElevatorCarProps> = ({
  elevator,
  onClick,
}) => {
  const bottomPosition = elevator.currentFloor * 60;
  const isDoorOpen = elevator.doorState === 'OPEN' || elevator.doorState === 'OPENING';

  const getDirectionIndicator = () => {
    if (elevator.direction === 'UP') return '↑';
    if (elevator.direction === 'DOWN') return '↓';
    return '○';
  };

  const getStateClass = () => {
    switch (elevator.state) {
      case 'MOVING': return 'moving';
      case 'DOOR_OPEN':
      case 'DOOR_OPENING': return 'doors-open';
      case 'DOOR_CLOSING': return 'doors-closing';
      default: return 'idle';
    }
  };

  return (
    <div
      className={`elevator-car ${getStateClass()} ${isDoorOpen ? 'open' : ''}`}
      style={{ bottom: `${bottomPosition}px` }}
      onClick={onClick}
      title={`Click to open control panel. Floor: ${elevator.currentFloor}, State: ${elevator.state}`}
    >
      <div className="car-display">
        <span className="floor-number">{elevator.currentFloor === 0 ? 'G' : elevator.currentFloor}</span>
        <span className="direction-indicator">{getDirectionIndicator()}</span>
      </div>
      <div className={`car-doors ${isDoorOpen ? 'open' : 'closed'}`}>
        <div className="door left"></div>
        <div className="door right"></div>
      </div>
      {elevator.destinations.length > 0 && (
        <div className="destination-indicator">
          {elevator.destinations.length}
        </div>
      )}
    </div>
  );
};
