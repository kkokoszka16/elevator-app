package com.elevator.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class DirectionTest {

    @ParameterizedTest(name = "opposite of {0} should be {1}")
    @CsvSource({
            "UP, DOWN",
            "DOWN, UP",
            "IDLE, IDLE"
    })
    void should_return_correct_opposite_direction(Direction input, Direction expected) {
        // when
        var result = input.opposite();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void should_return_true_when_direction_is_moving_up() {
        // given
        var direction = Direction.UP;

        // when / then
        assertThat(direction.isMoving()).isTrue();
    }

    @Test
    void should_return_true_when_direction_is_moving_down() {
        // given
        var direction = Direction.DOWN;

        // when / then
        assertThat(direction.isMoving()).isTrue();
    }

    @Test
    void should_return_false_when_direction_is_idle() {
        // given
        var direction = Direction.IDLE;

        // when / then
        assertThat(direction.isMoving()).isFalse();
    }

    @ParameterizedTest(name = "{0} isMoving should be consistent with opposite")
    @EnumSource(Direction.class)
    void should_maintain_moving_status_after_double_opposite(Direction direction) {
        // when
        var doubleOpposite = direction.opposite().opposite();

        // then
        assertThat(doubleOpposite).isEqualTo(direction);
    }
}
