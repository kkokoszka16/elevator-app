import React from 'react';
import { Elevator as ElevatorType } from '../../types/elevator';
import { ElevatorShaft } from '../Elevator/ElevatorShaft';
import { Floor } from './Floor';
import './Building.css';

interface BuildingProps {
  numberOfFloors: number;
  elevators: ElevatorType[];
  onCallElevator: (floor: number, direction: 'UP' | 'DOWN') => void;
  onSelectFloor: (elevatorId: number, floor: number) => void;
}

export const Building: React.FC<BuildingProps> = ({
  numberOfFloors,
  elevators,
  onCallElevator,
  onSelectFloor,
}) => {
  const floors = Array.from({ length: numberOfFloors }, (_, i) => numberOfFloors - 1 - i);

  return (
    <div className="building">
      <div className="building-content">
        <div className="floors-column">
          {floors.map((floor) => (
            <Floor
              key={floor}
              floorNumber={floor}
              isTopFloor={floor === numberOfFloors - 1}
              isGroundFloor={floor === 0}
              onCallUp={() => onCallElevator(floor, 'UP')}
              onCallDown={() => onCallElevator(floor, 'DOWN')}
            />
          ))}
        </div>
        <div className="shafts-container">
          {elevators.map((elevator) => (
            <ElevatorShaft
              key={elevator.id}
              elevator={elevator}
              numberOfFloors={numberOfFloors}
              onSelectFloor={(floor) => onSelectFloor(elevator.id, floor)}
            />
          ))}
        </div>
      </div>
    </div>
  );
};
