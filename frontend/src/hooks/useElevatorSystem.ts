import { useCallback, useEffect, useState } from 'react';
import { BuildingConfig, Elevator, ElevatorSystemStatus } from '../types/elevator';
import { useWebSocket } from './useWebSocket';

const API_BASE = '/api/v1';

export function useElevatorSystem() {
  const [elevators, setElevators] = useState<Elevator[]>([]);
  const [config, setConfig] = useState<BuildingConfig>({ numberOfFloors: 10, numberOfElevators: 3 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [simulationRunning, setSimulationRunning] = useState(false);

  const handleElevatorUpdate = useCallback((updatedElevators: Elevator[]) => {
    setElevators(updatedElevators);
  }, []);

  const { connected, callElevator: wsCallElevator, selectFloor: wsSelectFloor } = useWebSocket({
    onElevatorUpdate: handleElevatorUpdate,
  });

  const fetchStatus = useCallback(async () => {
    try {
      const response = await fetch(`${API_BASE}/elevators`);
      if (!response.ok) throw new Error('Failed to fetch elevator status');
      const data: ElevatorSystemStatus = await response.json();
      setElevators(data.elevators);
      setConfig({ numberOfFloors: data.numberOfFloors, numberOfElevators: data.numberOfElevators });
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchSimulationStatus = useCallback(async () => {
    try {
      const response = await fetch(`${API_BASE}/system/simulation/status`);
      if (response.ok) {
        const data = await response.json();
        setSimulationRunning(data.running);
      }
    } catch {
      console.error('Failed to fetch simulation status');
    }
  }, []);

  useEffect(() => {
    fetchStatus();
    fetchSimulationStatus();
  }, [fetchStatus, fetchSimulationStatus]);

  const callElevator = useCallback(async (floor: number, direction: 'UP' | 'DOWN') => {
    if (connected) {
      wsCallElevator(floor, direction);
    } else {
      try {
        await fetch(`${API_BASE}/elevators/call`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ floor, direction }),
        });
        await fetchStatus();
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to call elevator');
      }
    }
  }, [connected, wsCallElevator, fetchStatus]);

  const selectFloor = useCallback(async (elevatorId: number, floor: number) => {
    if (connected) {
      wsSelectFloor(elevatorId, floor);
    } else {
      try {
        await fetch(`${API_BASE}/elevators/${elevatorId}/select`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ floor }),
        });
        await fetchStatus();
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to select floor');
      }
    }
  }, [connected, wsSelectFloor, fetchStatus]);

  const updateConfig = useCallback(async (newConfig: BuildingConfig) => {
    try {
      const response = await fetch(`${API_BASE}/system/config`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newConfig),
      });
      if (!response.ok) throw new Error('Failed to update config');
      await fetchStatus();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update config');
    }
  }, [fetchStatus]);

  const resetSystem = useCallback(async () => {
    try {
      await fetch(`${API_BASE}/system/reset`, { method: 'POST' });
      await fetchStatus();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to reset system');
    }
  }, [fetchStatus]);

  const toggleSimulation = useCallback(async () => {
    try {
      const endpoint = simulationRunning ? 'stop' : 'start';
      const response = await fetch(`${API_BASE}/system/simulation/${endpoint}`, { method: 'POST' });
      if (response.ok) {
        const data = await response.json();
        setSimulationRunning(data.running);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to toggle simulation');
    }
  }, [simulationRunning]);

  return {
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
  };
}
