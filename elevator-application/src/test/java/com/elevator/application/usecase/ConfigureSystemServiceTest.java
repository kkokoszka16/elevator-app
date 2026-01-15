package com.elevator.application.usecase;

import com.elevator.application.port.out.BuildingConfigRepository;
import com.elevator.application.port.out.ElevatorEventPublisher;
import com.elevator.application.port.out.ElevatorRepository;
import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Elevator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ConfigureSystemServiceTest {

    @Mock
    private ElevatorRepository elevatorRepository;

    @Mock
    private BuildingConfigRepository configRepository;

    @Mock
    private ElevatorEventPublisher eventPublisher;

    private ConfigureSystemService service;

    private static final BuildingConfig DEFAULT_CONFIG = new BuildingConfig(
            10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2)
    );

    @BeforeEach
    void setUp() {
        service = new ConfigureSystemService(
                elevatorRepository,
                configRepository,
                eventPublisher
        );
    }

    @Nested
    class Configure {

        @Test
        void should_save_config() {
            // given
            var config = DEFAULT_CONFIG;

            // when
            service.configure(config);

            // then
            then(configRepository).should().save(config);
        }

        @Test
        void should_delete_all_existing_elevators() {
            // given
            var config = DEFAULT_CONFIG;

            // when
            service.configure(config);

            // then
            then(elevatorRepository).should().deleteAll();
        }

        @Test
        void should_create_correct_number_of_elevators() {
            // given
            var config = new BuildingConfig(10, 5, Duration.ofSeconds(3), Duration.ofSeconds(2));

            // when
            service.configure(config);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Elevator>> captor = ArgumentCaptor.forClass(List.class);
            then(elevatorRepository).should().saveAll(captor.capture());

            var elevators = captor.getValue();
            assertThat(elevators).hasSize(5);
        }

        @Test
        void should_create_elevators_with_correct_max_floor() {
            // given
            var config = new BuildingConfig(15, 2, Duration.ofSeconds(3), Duration.ofSeconds(2));

            // when
            service.configure(config);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Elevator>> captor = ArgumentCaptor.forClass(List.class);
            then(elevatorRepository).should().saveAll(captor.capture());

            var elevators = captor.getValue();
            for (var elevator : elevators) {
                assertThat(elevator.getCurrentFloor().number()).isZero();
            }
        }

        @Test
        void should_create_elevators_with_sequential_ids() {
            // given
            var config = new BuildingConfig(10, 4, Duration.ofSeconds(3), Duration.ofSeconds(2));

            // when
            service.configure(config);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Elevator>> captor = ArgumentCaptor.forClass(List.class);
            then(elevatorRepository).should().saveAll(captor.capture());

            var elevators = captor.getValue();
            for (int i = 0; i < 4; i++) {
                assertThat(elevators.get(i).getId().value()).isEqualTo(i);
            }
        }

        @Test
        void should_publish_state_update() {
            // given
            var config = DEFAULT_CONFIG;

            // when
            service.configure(config);

            // then
            then(eventPublisher).should().publishStateUpdate(any());
        }

        @ParameterizedTest(name = "should configure with {0} floors and {1} elevators")
        @CsvSource({
                "5, 2",
                "10, 3",
                "20, 5",
                "1, 1"
        })
        void should_configure_various_building_sizes(int floors, int elevators) {
            // given
            var config = new BuildingConfig(floors, elevators, Duration.ofSeconds(3), Duration.ofSeconds(2));

            // when
            service.configure(config);

            // then
            then(configRepository).should().save(config);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Elevator>> captor = ArgumentCaptor.forClass(List.class);
            then(elevatorRepository).should().saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(elevators);
        }
    }

    @Nested
    class Reset {

        @Test
        void should_save_default_config() {
            // given

            // when
            service.reset();

            // then
            then(configRepository).should().save(DEFAULT_CONFIG);
        }

        @Test
        void should_reinitialize_elevators() {
            // given

            // when
            service.reset();

            // then
            then(elevatorRepository).should().deleteAll();
            then(elevatorRepository).should().saveAll(any());
        }

        @Test
        void should_publish_state_update_after_reset() {
            // given

            // when
            service.reset();

            // then
            then(eventPublisher).should().publishStateUpdate(any());
        }

        @Test
        void should_reset_to_default_config() {
            // given

            // when
            service.reset();

            // then
            then(configRepository).should().save(DEFAULT_CONFIG);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Elevator>> captor = ArgumentCaptor.forClass(List.class);
            then(elevatorRepository).should().saveAll(captor.capture());
            assertThat(captor.getValue()).hasSize(DEFAULT_CONFIG.numberOfElevators());
        }
    }
}
