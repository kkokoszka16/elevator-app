import React, { useState } from 'react';
import { Elevator } from '../../types/elevator';
import { ElevatorCar } from './ElevatorCar';
import { ElevatorPanel } from './ElevatorPanel';
import './ElevatorShaft.css';

interface ElevatorShaftProps {
  elevator: Elevator;
  numberOfFloors: number;
  onSelectFloor: (floor: number) => void;
}

export const ElevatorShaft: React.FC<ElevatorShaftProps> = ({
  elevator,
  numberOfFloors,
  onSelectFloor,
}) => {
  const [showPanel, setShowPanel] = useState(false);

  const togglePanel = () => setShowPanel(!showPanel);

  return (
    <div className="elevator-shaft">
      <div className="shaft-header">
        <span className="elevator-id">E{elevator.id + 1}</span>
      </div>
      <div className="shaft-body" style={{ height: `${numberOfFloors * 60}px` }}>
        <ElevatorCar
          elevator={elevator}
          onClick={togglePanel}
        />
        {Array.from({ length: numberOfFloors }, (_, i) => (
          <div key={i} className="shaft-floor-marker" style={{ bottom: `${i * 60}px` }} />
        ))}
      </div>
      {showPanel && (
        <ElevatorPanel
          elevator={elevator}
          numberOfFloors={numberOfFloors}
          onSelectFloor={onSelectFloor}
          onClose={() => setShowPanel(false)}
        />
      )}
    </div>
  );
};
