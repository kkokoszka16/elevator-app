import React from 'react';
import './StatusDisplay.css';

interface StatusDisplayProps {
  connected: boolean;
  simulationRunning: boolean;
  numberOfFloors: number;
  numberOfElevators: number;
  onToggleSimulation: () => void;
  onReset: () => void;
}

export const StatusDisplay: React.FC<StatusDisplayProps> = ({
  connected,
  simulationRunning,
  numberOfFloors,
  numberOfElevators,
  onToggleSimulation,
  onReset,
}) => {
  return (
    <div className="status-display">
      <div className="status-indicators">
        <div className={`indicator ${connected ? 'connected' : 'disconnected'}`}>
          <span className="dot"></span>
          <span>{connected ? 'Connected' : 'Disconnected'}</span>
        </div>
        <div className={`indicator ${simulationRunning ? 'running' : 'stopped'}`}>
          <span className="dot"></span>
          <span>Simulation: {simulationRunning ? 'Running' : 'Stopped'}</span>
        </div>
      </div>
      <div className="status-info">
        <span>Floors: {numberOfFloors}</span>
        <span>Elevators: {numberOfElevators}</span>
      </div>
      <div className="status-controls">
        <button onClick={onToggleSimulation} className={simulationRunning ? 'stop' : 'start'}>
          {simulationRunning ? 'Stop' : 'Start'} Simulation
        </button>
        <button onClick={onReset} className="reset">
          Reset System
        </button>
      </div>
    </div>
  );
};
