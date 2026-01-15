import React, { useState } from 'react';
import { BuildingConfig } from '../../types/elevator';
import './ConfigPanel.css';

interface ConfigPanelProps {
  config: BuildingConfig;
  onUpdateConfig: (config: BuildingConfig) => void;
}

export const ConfigPanel: React.FC<ConfigPanelProps> = ({ config, onUpdateConfig }) => {
  const [floors, setFloors] = useState(config.numberOfFloors);
  const [elevators, setElevators] = useState(config.numberOfElevators);
  const [isOpen, setIsOpen] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onUpdateConfig({
      numberOfFloors: floors,
      numberOfElevators: elevators,
      doorOpenDurationSeconds: 3,
      floorTravelDurationSeconds: 2,
    });
    setIsOpen(false);
  };

  if (!isOpen) {
    return (
      <button className="config-toggle" onClick={() => setIsOpen(true)}>
        Configure Building
      </button>
    );
  }

  return (
    <div className="config-panel">
      <h3>Building Configuration</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="floors">Number of Floors:</label>
          <input
            type="number"
            id="floors"
            min="2"
            max="50"
            value={floors}
            onChange={(e) => setFloors(parseInt(e.target.value) || 2)}
          />
        </div>
        <div className="form-group">
          <label htmlFor="elevators">Number of Elevators:</label>
          <input
            type="number"
            id="elevators"
            min="1"
            max="10"
            value={elevators}
            onChange={(e) => setElevators(parseInt(e.target.value) || 1)}
          />
        </div>
        <div className="form-actions">
          <button type="submit">Apply</button>
          <button type="button" onClick={() => setIsOpen(false)}>Cancel</button>
        </div>
      </form>
    </div>
  );
};
