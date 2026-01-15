import React from 'react';
import { Elevator } from '../../types/elevator';
import './ElevatorPanel.css';

interface ElevatorPanelProps {
  elevator: Elevator;
  numberOfFloors: number;
  onSelectFloor: (floor: number) => void;
  onClose: () => void;
}

export const ElevatorPanel: React.FC<ElevatorPanelProps> = ({
  elevator,
  numberOfFloors,
  onSelectFloor,
  onClose,
}) => {
  const floors = Array.from({ length: numberOfFloors }, (_, i) => numberOfFloors - 1 - i);

  return (
    <div className="elevator-panel-overlay" onClick={onClose}>
      <div className="elevator-panel" onClick={(e) => e.stopPropagation()}>
        <div className="panel-header">
          <h3>Elevator {elevator.id + 1}</h3>
          <button className="close-button" onClick={onClose}>X</button>
        </div>
        <div className="panel-display">
          <span className="current-floor">
            {elevator.currentFloor === 0 ? 'G' : elevator.currentFloor}
          </span>
          <span className="direction">
            {elevator.direction === 'UP' ? '↑' : elevator.direction === 'DOWN' ? '↓' : '—'}
          </span>
        </div>
        <div className="panel-status">
          <span className={`status ${elevator.state.toLowerCase()}`}>
            {elevator.state.replace('_', ' ')}
          </span>
          <span className={`door-state ${elevator.doorState.toLowerCase()}`}>
            Doors: {elevator.doorState}
          </span>
        </div>
        <div className="panel-buttons">
          {floors.map((floor) => {
            const isCurrentFloor = floor === elevator.currentFloor;
            const isDestination = elevator.destinations.includes(floor);
            return (
              <button
                key={floor}
                className={`floor-button ${isCurrentFloor ? 'current' : ''} ${isDestination ? 'selected' : ''}`}
                onClick={() => onSelectFloor(floor)}
                disabled={isCurrentFloor}
              >
                {floor === 0 ? 'G' : floor}
              </button>
            );
          })}
        </div>
        {elevator.destinations.length > 0 && (
          <div className="panel-destinations">
            <span>Queue: {elevator.destinations.join(', ')}</span>
          </div>
        )}
      </div>
    </div>
  );
};
