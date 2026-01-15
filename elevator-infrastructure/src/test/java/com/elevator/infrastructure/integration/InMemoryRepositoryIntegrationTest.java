package com.elevator.infrastructure.integration;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.DoorState;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.ElevatorState;
import com.elevator.domain.model.Floor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("InMemory Repository Integration Tests")
class InMemoryRepositoryIntegrationTest {

    @Autowired
    private ElevatorRepository elevatorRepository;

    @Autowired
    private BuildingConfigRepository configRepository;

    @BeforeEach
    void setUp() {
        elevatorRepository.deleteAll();
    }

    @Test
    @DisplayName("should save and retrieve elevator by id")
    void should_save_and_retrieve_elevator_by_id() {
        var elevator = createElevator(1, 5);

        elevatorRepository.save(elevator);

        var retrieved = elevatorRepository.findById(new ElevatorId(1));

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(new ElevatorId(1));
        assertThat(retrieved.get().getCurrentFloor()).isEqualTo(new Floor(5));
    }

    @Test
    @DisplayName("should return empty when elevator not found")
    void should_return_empty_when_elevator_not_found() {
        var result = elevatorRepository.findById(new ElevatorId(999));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should save multiple elevators and find all")
    void should_save_multiple_elevators_and_find_all() {
        var elevator1 = createElevator(1, 3);
        var elevator2 = createElevator(2, 7);
        var elevator3 = createElevator(3, 1);

        elevatorRepository.saveAll(List.of(elevator1, elevator2, elevator3));

        var allElevators = elevatorRepository.findAll();

        assertThat(allElevators).hasSize(3);
        assertThat(allElevators)
                .extracting(Elevator::getId)
                .containsExactlyInAnyOrder(
                        new ElevatorId(1),
                        new ElevatorId(2),
                        new ElevatorId(3)
                );
    }

    @Test
    @DisplayName("should update existing elevator")
    void should_update_existing_elevator() {
        var elevator = createElevator(1, 5);
        elevatorRepository.save(elevator);

        elevator.addDestination(new Floor(8));
        elevatorRepository.save(elevator);

        var retrieved = elevatorRepository.findById(new ElevatorId(1));

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDestinations()).contains(new Floor(8));
    }

    @Test
    @DisplayName("should delete all elevators")
    void should_delete_all_elevators() {
        var elevator1 = createElevator(1, 3);
        var elevator2 = createElevator(2, 7);

        elevatorRepository.saveAll(List.of(elevator1, elevator2));
        assertThat(elevatorRepository.findAll()).hasSize(2);

        elevatorRepository.deleteAll();

        assertThat(elevatorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("should handle concurrent saves correctly")
    void should_handle_concurrent_saves_correctly() {
        var elevator1 = createElevator(1, 3);
        var elevator2 = createElevator(2, 7);
        var elevator3 = createElevator(1, 9);

        elevatorRepository.save(elevator1);
        elevatorRepository.save(elevator2);
        elevatorRepository.save(elevator3);

        var allElevators = elevatorRepository.findAll();

        assertThat(allElevators).hasSize(2);

        var elevator1Updated = elevatorRepository.findById(new ElevatorId(1));
        assertThat(elevator1Updated).isPresent();
        assertThat(elevator1Updated.get().getCurrentFloor()).isEqualTo(new Floor(9));
    }

    @Test
    @DisplayName("should persist elevator state changes")
    void should_persist_elevator_state_changes() {
        var elevator = createElevator(1, 5);
        elevator.addDestination(new Floor(8));
        elevator.addDestination(new Floor(10));

        elevatorRepository.save(elevator);

        var retrieved = elevatorRepository.findById(new ElevatorId(1));

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDestinations())
                .hasSize(2)
                .containsExactlyInAnyOrder(new Floor(8), new Floor(10));
        assertThat(retrieved.get().getState()).isEqualTo(ElevatorState.IDLE);
    }

    @Test
    @DisplayName("should retrieve building config from repository")
    void should_retrieve_building_config_from_repository() {
        var config = configRepository.get();

        assertThat(config).isNotNull();
        assertThat(config.numberOfFloors()).isPositive();
        assertThat(config.numberOfElevators()).isPositive();
    }

    @Test
    @DisplayName("should update building config")
    void should_update_building_config() {
        var newConfig = new BuildingConfig(15, 5,
                java.time.Duration.ofSeconds(3), java.time.Duration.ofSeconds(2));

        configRepository.save(newConfig);

        var retrieved = configRepository.get();

        assertThat(retrieved.numberOfFloors()).isEqualTo(15);
        assertThat(retrieved.numberOfElevators()).isEqualTo(5);
    }

    @Test
    @DisplayName("should validate building config constraints")
    void should_validate_building_config_constraints() {
        var config = configRepository.get();

        assertThat(config.isValidFloor(new Floor(1))).isTrue();
        assertThat(config.isValidFloor(new Floor(config.numberOfFloors() - 1))).isTrue();
        assertThat(config.numberOfFloors()).isPositive();
        assertThat(config.numberOfElevators()).isPositive();
    }

    @Test
    @DisplayName("should persist complex elevator state with multiple destinations")
    void should_persist_complex_elevator_state_with_multiple_destinations() {
        var elevator = new Elevator(new ElevatorId(1), 15);

        elevator.addDestination(new Floor(7));
        elevator.addDestination(new Floor(9));
        elevator.addDestination(new Floor(12));

        elevatorRepository.save(elevator);

        var retrieved = elevatorRepository.findById(new ElevatorId(1));

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDirection()).isIn(Direction.UP, Direction.IDLE);
        assertThat(retrieved.get().getDestinations()).hasSize(3);
    }

    private Elevator createElevator(int id, int currentFloor) {
        var elevator = new Elevator(new ElevatorId(id), 15);
        for (int i = 0; i < currentFloor; i++) {
            elevator.moveUp();
        }
        if (currentFloor > 0) {
            elevator.stop();
            elevator.openDoors();
            elevator.doorsOpened();
            elevator.closeDoors();
            elevator.doorsClosed();
        }
        return elevator;
    }
}
