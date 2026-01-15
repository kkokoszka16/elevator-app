import React from 'react';
import { Building } from './components/Building/Building';
import { StatusDisplay } from './components/Controls/StatusDisplay';
import { ConfigPanel } from './components/Controls/ConfigPanel';
import { useElevatorSystem } from './hooks/useElevatorSystem';
import './styles/App.css';

const App: React.FC = () => {
  const {
    elevators,
    config,
    loading,
    error,
    connected,
    simulationRunning,
    callElevator,
    selectFloor,
    updateConfig,
    resetSystem,
    toggleSimulation,
  } = useElevatorSystem();

  if (loading) {
    return (
      <div className="app loading">
        <div className="loader">Loading elevator system...</div>
      </div>
    );
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Elevator System Simulation</h1>
        <StatusDisplay
          connected={connected}
          simulationRunning={simulationRunning}
          numberOfFloors={config.numberOfFloors}
          numberOfElevators={config.numberOfElevators}
          onToggleSimulation={toggleSimulation}
          onReset={resetSystem}
        />
      </header>

      {error && <div className="error-banner">{error}</div>}

      <main className="app-main">
        <Building
          numberOfFloors={config.numberOfFloors}
          elevators={elevators}
          onCallElevator={callElevator}
          onSelectFloor={selectFloor}
        />
      </main>

      <aside className="app-sidebar">
        <ConfigPanel config={config} onUpdateConfig={updateConfig} />
        <div className="instructions">
          <h3>Instructions</h3>
          <ul>
            <li>Use UP/DOWN buttons on each floor to call an elevator</li>
            <li>Click on an elevator car to open its control panel</li>
            <li>Select destination floors from the panel</li>
          </ul>
        </div>
      </aside>
    </div>
  );
};

export default App;
