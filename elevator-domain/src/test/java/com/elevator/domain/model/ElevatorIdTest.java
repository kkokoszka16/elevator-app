package com.elevator.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElevatorIdTest {

    @ParameterizedTest(name = "should_create_valid_elevator_id_when_value_is_{0}")
    @ValueSource(ints = {0, 1, 5, 10, 100})
    void should_create_valid_elevator_id_when_value_is_non_negative(int value) {
        // given / when
        var elevatorId = new ElevatorId(value);

        // then
        assertThat(elevatorId.value()).isEqualTo(value);
    }

    @ParameterizedTest(name = "should_throw_exception_when_elevator_id_is_{0}")
    @ValueSource(ints = {-1, -5, -100, Integer.MIN_VALUE})
    void should_throw_exception_when_elevator_id_is_negative(int value) {
        // given / when / then
        assertThatThrownBy(() -> new ElevatorId(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void should_be_equal_when_same_value() {
        // given
        var id1 = new ElevatorId(5);
        var id2 = new ElevatorId(5);

        // then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void should_not_be_equal_when_different_value() {
        // given
        var id1 = new ElevatorId(5);
        var id2 = new ElevatorId(6);

        // then
        assertThat(id1).isNotEqualTo(id2);
    }
}
