package com.elevator.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ElevatorStateTest {

    @ParameterizedTest(name = "{0} isIdle should be {1}")
    @CsvSource({
            "IDLE, true",
            "MOVING, false",
            "STOPPED, false",
            "DOOR_OPENING, false",
            "DOOR_OPEN, false",
            "DOOR_CLOSING, false"
    })
    void should_correctly_identify_idle_state(ElevatorState state, boolean expected) {
        // when
        var result = state.isIdle();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} isMoving should be {1}")
    @CsvSource({
            "IDLE, false",
            "MOVING, true",
            "STOPPED, false",
            "DOOR_OPENING, false",
            "DOOR_OPEN, false",
            "DOOR_CLOSING, false"
    })
    void should_correctly_identify_moving_state(ElevatorState state, boolean expected) {
        // when
        var result = state.isMoving();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} canAcceptNewDestination should always be true")
    @EnumSource(ElevatorState.class)
    void should_always_accept_new_destination(ElevatorState state) {
        // when
        var result = state.canAcceptNewDestination();

        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest(name = "{0} should have mutually exclusive idle and moving states")
    @EnumSource(ElevatorState.class)
    void should_have_mutually_exclusive_states(ElevatorState state) {
        // given
        var isIdle = state.isIdle();
        var isMoving = state.isMoving();

        // then
        assertThat(isIdle && isMoving).isFalse();
    }
}
