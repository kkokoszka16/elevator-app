package com.elevator.e2e;

import com.elevator.application.port.in.CallElevatorUseCase;
import com.elevator.application.port.in.ConfigureSystemUseCase;
import com.elevator.application.port.in.GetSystemStatusUseCase;
import com.elevator.application.port.in.SelectFloorUseCase;
import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Elevator System E2E Tests")
class ElevatorSystemE2ETest {

    @Autowired
    private ConfigureSystemUseCase configureSystemUseCase;

    @Autowired
    private CallElevatorUseCase callElevatorUseCase;

    @Autowired
    private SelectFloorUseCase selectFloorUseCase;

    @Autowired
    private GetSystemStatusUseCase getSystemStatusUseCase;

    @BeforeEach
    void setUp() {
        configureSystemUseCase.reset();
    }

    @Test
    @DisplayName("should complete full call and select floor flow")
    void should_complete_full_call_and_select_floor_flow() {
        var config = getSystemStatusUseCase.getConfig();
        assertThat(config.numberOfFloors()).isEqualTo(10);
        assertThat(config.numberOfElevators()).isEqualTo(3);

        var initialElevators = getSystemStatusUseCase.getAllElevators();
        assertThat(initialElevators).hasSize(3);

        callElevatorUseCase.callElevator(new Floor(5), Direction.UP);

        var elevatorsAfterCall = getSystemStatusUseCase.getAllElevators();
        var hasDestination = elevatorsAfterCall.stream()
                .anyMatch(e -> e.getDestinations().contains(new Floor(5)));
        assertThat(hasDestination).isTrue();

        var assignedElevator = elevatorsAfterCall.stream()
                .filter(e -> e.getDestinations().contains(new Floor(5)))
                .findFirst()
                .orElseThrow();

        selectFloorUseCase.selectFloor(assignedElevator.getId(), new Floor(8));

        var elevatorsAfterSelect = getSystemStatusUseCase.getAllElevators();
        var updatedElevator = elevatorsAfterSelect.stream()
                .filter(e -> e.getId().equals(assignedElevator.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(updatedElevator.getDestinations())
                .contains(new Floor(5), new Floor(8));
    }

    @Test
    @DisplayName("should handle multiple concurrent calls")
    void should_handle_multiple_concurrent_calls() {
        callElevatorUseCase.callElevator(new Floor(2), Direction.UP);
        callElevatorUseCase.callElevator(new Floor(7), Direction.DOWN);
        callElevatorUseCase.callElevator(new Floor(9), Direction.DOWN);

        var elevators = getSystemStatusUseCase.getAllElevators();

        var totalDestinations = elevators.stream()
                .mapToLong(e -> e.getDestinations().size())
                .sum();

        assertThat(totalDestinations).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("should reconfigure system and maintain consistency")
    void should_reconfigure_system_and_maintain_consistency() {
        var newConfig = new BuildingConfig(15, 5,
                java.time.Duration.ofSeconds(3), java.time.Duration.ofSeconds(2));

        configureSystemUseCase.configure(newConfig);

        var config = getSystemStatusUseCase.getConfig();
        assertThat(config.numberOfFloors()).isEqualTo(15);
        assertThat(config.numberOfElevators()).isEqualTo(5);

        var elevators = getSystemStatusUseCase.getAllElevators();
        assertThat(elevators).hasSize(5);

        elevators.forEach(elevator -> {
            assertThat(elevator.getCurrentFloor().number())
                    .isBetween(0, 14);
        });
    }

    @Test
    @DisplayName("should handle edge case with all elevators on same floor")
    void should_handle_edge_case_with_all_elevators_on_same_floor() {
        configureSystemUseCase.reset();

        callElevatorUseCase.callElevator(new Floor(5), Direction.UP);

        var elevators = getSystemStatusUseCase.getAllElevators();
        var assignedCount = elevators.stream()
                .filter(e -> !e.getDestinations().isEmpty())
                .count();

        assertThat(assignedCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("should validate floor boundaries throughout system")
    void should_validate_floor_boundaries_throughout_system() {
        var config = getSystemStatusUseCase.getConfig();

        assertThatThrownBy(() ->
                callElevatorUseCase.callElevator(new Floor(-1), Direction.UP))
                .isInstanceOf(InvalidFloorException.class);

        assertThatThrownBy(() ->
                callElevatorUseCase.callElevator(new Floor(config.numberOfFloors()), Direction.UP))
                .isInstanceOf(InvalidFloorException.class);

        assertThatThrownBy(() ->
                selectFloorUseCase.selectFloor(new ElevatorId(1), new Floor(-1)))
                .isInstanceOf(InvalidFloorException.class);

        assertThatThrownBy(() ->
                selectFloorUseCase.selectFloor(new ElevatorId(1), new Floor(config.numberOfFloors())))
                .isInstanceOf(InvalidFloorException.class);
    }

    @Test
    @DisplayName("should throw exception for non-existent elevator")
    void should_throw_exception_for_non_existent_elevator() {
        assertThatThrownBy(() ->
                selectFloorUseCase.selectFloor(new ElevatorId(999), new Floor(5)))
                .isInstanceOf(ElevatorNotFoundException.class)
                .hasMessageContaining("999");

        assertThat(getSystemStatusUseCase.getElevator(new ElevatorId(999)))
                .isEmpty();
    }

    @Test
    @DisplayName("should maintain elevator state consistency across operations")
    void should_maintain_elevator_state_consistency_across_operations() {
        var elevator1 = getSystemStatusUseCase.getElevator(new ElevatorId(1))
                .orElseThrow();

        selectFloorUseCase.selectFloor(new ElevatorId(1), new Floor(7));

        var elevator1Updated = getSystemStatusUseCase.getElevator(new ElevatorId(1))
                .orElseThrow();

        assertThat(elevator1Updated.getDestinations())
                .contains(new Floor(7));
        assertThat(elevator1Updated.getId()).isEqualTo(elevator1.getId());
    }

    @Test
    @DisplayName("should handle extreme load with multiple destinations")
    void should_handle_extreme_load_with_multiple_destinations() {
        var elevatorId = new ElevatorId(1);

        selectFloorUseCase.selectFloor(elevatorId, new Floor(3));
        selectFloorUseCase.selectFloor(elevatorId, new Floor(5));
        selectFloorUseCase.selectFloor(elevatorId, new Floor(7));
        selectFloorUseCase.selectFloor(elevatorId, new Floor(9));

        var elevator = getSystemStatusUseCase.getElevator(elevatorId)
                .orElseThrow();

        assertThat(elevator.getDestinations()).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("should distribute load across multiple elevators")
    void should_distribute_load_across_multiple_elevators() {
        callElevatorUseCase.callElevator(new Floor(1), Direction.UP);
        callElevatorUseCase.callElevator(new Floor(5), Direction.UP);
        callElevatorUseCase.callElevator(new Floor(9), Direction.DOWN);

        var elevators = getSystemStatusUseCase.getAllElevators();

        var elevatorsWithDestinations = elevators.stream()
                .filter(e -> !e.getDestinations().isEmpty())
                .count();

        assertThat(elevatorsWithDestinations).isGreaterThan(0);
    }

    @Test
    @DisplayName("should reset system to default state")
    void should_reset_system_to_default_state() {
        callElevatorUseCase.callElevator(new Floor(5), Direction.UP);
        selectFloorUseCase.selectFloor(new ElevatorId(1), new Floor(8));

        configureSystemUseCase.reset();

        var config = getSystemStatusUseCase.getConfig();
        assertThat(config.numberOfFloors()).isEqualTo(10);
        assertThat(config.numberOfElevators()).isEqualTo(3);

        var elevators = getSystemStatusUseCase.getAllElevators();
        assertThat(elevators).hasSize(3);

        elevators.forEach(elevator -> {
            assertThat(elevator.getCurrentFloor()).isEqualTo(new Floor(0));
        });
    }

    @Test
    @DisplayName("should handle reconfiguration with more elevators")
    void should_handle_reconfiguration_with_more_elevators() {
        var originalElevators = getSystemStatusUseCase.getAllElevators();
        assertThat(originalElevators).hasSize(3);

        var newConfig = new BuildingConfig(10, 6,
                java.time.Duration.ofSeconds(3), java.time.Duration.ofSeconds(2));
        configureSystemUseCase.configure(newConfig);

        var newElevators = getSystemStatusUseCase.getAllElevators();
        assertThat(newElevators).hasSize(6);
    }

    @Test
    @DisplayName("should retrieve individual elevator status")
    void should_retrieve_individual_elevator_status() {
        var elevator = getSystemStatusUseCase.getElevator(new ElevatorId(1));

        assertThat(elevator).isPresent();
        assertThat(elevator.get().getId()).isEqualTo(new ElevatorId(1));
        assertThat(elevator.get().getCurrentFloor()).isNotNull();
        assertThat(elevator.get().getState()).isNotNull();
        assertThat(elevator.get().getDirection()).isNotNull();
        assertThat(elevator.get().getDoorState()).isNotNull();
    }
}
