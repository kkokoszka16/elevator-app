import React from 'react';
import './Floor.css';

interface FloorProps {
  floorNumber: number;
  isTopFloor: boolean;
  isGroundFloor: boolean;
  onCallUp: () => void;
  onCallDown: () => void;
}

export const Floor: React.FC<FloorProps> = ({
  floorNumber,
  isTopFloor,
  isGroundFloor,
  onCallUp,
  onCallDown,
}) => {
  return (
    <div className="floor">
      <div className="floor-label">
        {floorNumber === 0 ? 'G' : floorNumber}
      </div>
      <div className="floor-buttons">
        {!isTopFloor && (
          <button className="call-button up" onClick={onCallUp} title="Call elevator going up">
            <span className="arrow">&#9650;</span>
          </button>
        )}
        {!isGroundFloor && (
          <button className="call-button down" onClick={onCallDown} title="Call elevator going down">
            <span className="arrow">&#9660;</span>
          </button>
        )}
      </div>
    </div>
  );
};
