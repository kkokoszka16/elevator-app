package com.elevator.domain.model;

import com.elevator.domain.exception.InvalidFloorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloorTest {

    @ParameterizedTest(name = "should_create_valid_floor_when_number_is_{0}")
    @ValueSource(ints = {0, 1, 5, 10, 100})
    void should_create_valid_floor_when_number_is_non_negative(int floorNumber) {
        // given / when
        var floor = new Floor(floorNumber);

        // then
        assertThat(floor.number()).isEqualTo(floorNumber);
    }

    @ParameterizedTest(name = "should_throw_exception_when_floor_number_is_{0}")
    @ValueSource(ints = {-1, -5, -100, Integer.MIN_VALUE})
    void should_throw_exception_when_floor_number_is_negative(int floorNumber) {
        // given / when / then
        assertThatThrownBy(() -> new Floor(floorNumber))
                .isInstanceOf(InvalidFloorException.class)
                .hasMessageContaining("Invalid floor number");
    }

    @Test
    void should_return_floor_above_when_calling_above() {
        // given
        var floor = new Floor(5);

        // when
        var result = floor.above();

        // then
        assertThat(result.number()).isEqualTo(6);
    }

    @Test
    void should_return_floor_below_when_calling_below() {
        // given
        var floor = new Floor(5);

        // when
        var result = floor.below();

        // then
        assertThat(result.number()).isEqualTo(4);
    }

    @Test
    void should_throw_exception_when_calling_below_from_ground_floor() {
        // given
        var floor = new Floor(0);

        // when / then
        assertThatThrownBy(floor::below)
                .isInstanceOf(InvalidFloorException.class);
    }

    @ParameterizedTest(name = "distance from {0} to {1} should be {2}")
    @CsvSource({
            "0, 5, 5",
            "5, 0, 5",
            "3, 3, 0",
            "10, 7, 3",
            "2, 8, 6"
    })
    void should_calculate_correct_distance_between_floors(int from, int to, int expectedDistance) {
        // given
        var floorFrom = new Floor(from);
        var floorTo = new Floor(to);

        // when
        var result = floorFrom.distanceTo(floorTo);

        // then
        assertThat(result).isEqualTo(expectedDistance);
    }

    @ParameterizedTest(name = "floor {0} isAbove floor {1} should be {2}")
    @CsvSource({
            "5, 3, true",
            "3, 5, false",
            "5, 5, false",
            "10, 0, true",
            "0, 10, false"
    })
    void should_correctly_determine_if_floor_is_above(int first, int second, boolean expected) {
        // given
        var floor1 = new Floor(first);
        var floor2 = new Floor(second);

        // when
        var result = floor1.isAbove(floor2);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "floor {0} isBelow floor {1} should be {2}")
    @CsvSource({
            "3, 5, true",
            "5, 3, false",
            "5, 5, false",
            "0, 10, true",
            "10, 0, false"
    })
    void should_correctly_determine_if_floor_is_below(int first, int second, boolean expected) {
        // given
        var floor1 = new Floor(first);
        var floor2 = new Floor(second);

        // when
        var result = floor1.isBelow(floor2);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void should_compare_floors_correctly() {
        // given
        var floor3 = new Floor(3);
        var floor5 = new Floor(5);
        var floor5Another = new Floor(5);

        // then
        assertThat(floor3.compareTo(floor5)).isLessThan(0);
        assertThat(floor5.compareTo(floor3)).isGreaterThan(0);
        assertThat(floor5.compareTo(floor5Another)).isZero();
    }

    @Test
    void should_be_equal_when_same_floor_number() {
        // given
        var floor1 = new Floor(5);
        var floor2 = new Floor(5);

        // then
        assertThat(floor1).isEqualTo(floor2);
        assertThat(floor1.hashCode()).isEqualTo(floor2.hashCode());
    }

    @Test
    void should_not_be_equal_when_different_floor_number() {
        // given
        var floor1 = new Floor(5);
        var floor2 = new Floor(6);

        // then
        assertThat(floor1).isNotEqualTo(floor2);
    }
}
