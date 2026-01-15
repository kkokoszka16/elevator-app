package com.elevator.application.usecase;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.ElevatorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GetSystemStatusServiceTest {

    @Mock
    private ElevatorRepository elevatorRepository;

    @Mock
    private BuildingConfigRepository configRepository;

    private GetSystemStatusService service;

    private static final BuildingConfig DEFAULT_CONFIG = new BuildingConfig(
            10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2)
    );

    @BeforeEach
    void setUp() {
        service = new GetSystemStatusService(elevatorRepository, configRepository);
    }

    @Nested
    class GetAllElevators {

        @Test
        void should_return_all_elevators() {
            // given
            var elevator1 = new Elevator(new ElevatorId(0), 9);
            var elevator2 = new Elevator(new ElevatorId(1), 9);
            var elevators = List.of(elevator1, elevator2);

            given(elevatorRepository.findAll()).willReturn(elevators);

            // when
            var result = service.getAllElevators();

            // then
            assertThat(result).hasSize(2);
            then(elevatorRepository).should().findAll();
        }

        @Test
        void should_return_empty_list_when_no_elevators() {
            // given
            given(elevatorRepository.findAll()).willReturn(Collections.emptyList());

            // when
            var result = service.getAllElevators();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetElevator {

        @Test
        void should_return_elevator_when_exists() {
            // given
            var elevatorId = new ElevatorId(0);
            var elevator = new Elevator(elevatorId, 9);

            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.of(elevator));

            // when
            var result = service.getElevator(elevatorId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(elevatorId);
        }

        @Test
        void should_return_empty_when_elevator_not_exists() {
            // given
            var elevatorId = new ElevatorId(99);

            given(elevatorRepository.findById(elevatorId)).willReturn(Optional.empty());

            // when
            var result = service.getElevator(elevatorId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetConfig {

        @Test
        void should_return_building_config() {
            // given
            given(configRepository.get()).willReturn(DEFAULT_CONFIG);

            // when
            var result = service.getConfig();

            // then
            assertThat(result).isEqualTo(DEFAULT_CONFIG);
            assertThat(result.numberOfFloors()).isEqualTo(10);
            assertThat(result.numberOfElevators()).isEqualTo(3);
        }
    }
}
