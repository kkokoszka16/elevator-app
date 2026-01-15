package com.elevator.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuildingConfigTest {

    @Test
    void should_create_valid_config_with_valid_parameters() {
        // given
        var floors = 10;
        var elevators = 3;
        var doorOpenDuration = Duration.ofSeconds(3);
        var floorTravelDuration = Duration.ofSeconds(2);

        // when
        var config = new BuildingConfig(floors, elevators, doorOpenDuration, floorTravelDuration);

        // then
        assertThat(config.numberOfFloors()).isEqualTo(floors);
        assertThat(config.numberOfElevators()).isEqualTo(elevators);
        assertThat(config.doorOpenDuration()).isEqualTo(doorOpenDuration);
        assertThat(config.floorTravelDuration()).isEqualTo(floorTravelDuration);
    }

    @ParameterizedTest(name = "should_throw_exception_when_floors_is_{0}")
    @ValueSource(ints = {0, -1, -10, Integer.MIN_VALUE})
    void should_throw_exception_when_floors_is_invalid(int floors) {
        // given
        var doorOpenDuration = Duration.ofSeconds(3);
        var floorTravelDuration = Duration.ofSeconds(2);

        // when / then
        assertThatThrownBy(() -> new BuildingConfig(floors, 3, doorOpenDuration, floorTravelDuration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("floors");
    }

    @ParameterizedTest(name = "should_throw_exception_when_elevators_is_{0}")
    @ValueSource(ints = {0, -1, -10, Integer.MIN_VALUE})
    void should_throw_exception_when_elevators_is_invalid(int elevators) {
        // given
        var doorOpenDuration = Duration.ofSeconds(3);
        var floorTravelDuration = Duration.ofSeconds(2);

        // when / then
        assertThatThrownBy(() -> new BuildingConfig(10, elevators, doorOpenDuration, floorTravelDuration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("elevators");
    }

    @Test
    void should_throw_exception_when_door_open_duration_is_null() {
        // given
        var floorTravelDuration = Duration.ofSeconds(2);

        // when / then
        assertThatThrownBy(() -> new BuildingConfig(10, 3, null, floorTravelDuration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Door open duration");
    }

    @Test
    void should_throw_exception_when_door_open_duration_is_zero() {
        // given
        var floorTravelDuration = Duration.ofSeconds(2);

        // when / then
        assertThatThrownBy(() -> new BuildingConfig(10, 3, Duration.ZERO, floorTravelDuration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Door open duration");
    }

    @Test
    void should_throw_exception_when_door_open_duration_is_negative() {
        // given
        var floorTravelDuration = Duration.ofSeconds(2);

        // when / then
        assertThatThrownBy(() -> new BuildingConfig(10, 3, Duration.ofSeconds(-1), floorTravelDuration))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Door open duration");
    }

    @Test
    void should_throw_exception_when_floor_travel_duration_is_null() {
        // given
        var doorOpenDuration = Duration.ofSeconds(3);

        // when / then
        assertThatThrownBy(() -> new BuildingConfig(10, 3, doorOpenDuration, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Floor travel duration");
    }

    @Test
    void should_throw_exception_when_floor_travel_duration_is_zero() {
        // given
        var doorOpenDuration = Duration.ofSeconds(3);

        // when / then
        assertThatThrownBy(() -> new BuildingConfig(10, 3, doorOpenDuration, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Floor travel duration");
    }

    @Test
    void should_throw_exception_when_floor_travel_duration_is_negative() {
        // given
        var doorOpenDuration = Duration.ofSeconds(3);

        // when / then
        assertThatThrownBy(() -> new BuildingConfig(10, 3, doorOpenDuration, Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Floor travel duration");
    }

    @Test
    void should_return_correct_default_config() {
        // when
        var config = BuildingConfig.defaultConfig();

        // then
        assertThat(config.numberOfFloors()).isEqualTo(10);
        assertThat(config.numberOfElevators()).isEqualTo(3);
        assertThat(config.doorOpenDuration()).isEqualTo(Duration.ofSeconds(3));
        assertThat(config.floorTravelDuration()).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void should_return_correct_top_floor() {
        // given
        var config = new BuildingConfig(10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2));

        // when
        var topFloor = config.topFloor();

        // then
        assertThat(topFloor.number()).isEqualTo(9);
    }

    @Test
    void should_return_correct_ground_floor() {
        // given
        var config = new BuildingConfig(10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2));

        // when
        var groundFloor = config.groundFloor();

        // then
        assertThat(groundFloor.number()).isZero();
    }

    @ParameterizedTest(name = "floor {0} in 10-floor building should be valid: {1}")
    @CsvSource({
            "0, true",
            "5, true",
            "9, true",
            "10, false",
            "11, false"
    })
    void should_correctly_validate_floor(int floorNumber, boolean expected) {
        // given
        var config = new BuildingConfig(10, 3, Duration.ofSeconds(3), Duration.ofSeconds(2));
        var floor = new Floor(floorNumber);

        // when
        var result = config.isValidFloor(floor);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void should_handle_single_floor_building() {
        // given
        var config = new BuildingConfig(1, 1, Duration.ofSeconds(1), Duration.ofSeconds(1));

        // when / then
        assertThat(config.topFloor().number()).isZero();
        assertThat(config.groundFloor().number()).isZero();
        assertThat(config.isValidFloor(new Floor(0))).isTrue();
    }
}
