package com.elevator.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class DoorStateTest {

    @ParameterizedTest(name = "{0} canMove should be {1}")
    @CsvSource({
            "OPEN, false",
            "CLOSED, true",
            "OPENING, false",
            "CLOSING, false"
    })
    void should_only_allow_movement_when_door_is_closed(DoorState state, boolean expected) {
        // when
        var result = state.canMove();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} isFullyOpen should be {1}")
    @CsvSource({
            "OPEN, true",
            "CLOSED, false",
            "OPENING, false",
            "CLOSING, false"
    })
    void should_correctly_identify_fully_open_state(DoorState state, boolean expected) {
        // when
        var result = state.isFullyOpen();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} isFullyClosed should be {1}")
    @CsvSource({
            "OPEN, false",
            "CLOSED, true",
            "OPENING, false",
            "CLOSING, false"
    })
    void should_correctly_identify_fully_closed_state(DoorState state, boolean expected) {
        // when
        var result = state.isFullyClosed();

        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "DoorState {0} should have consistent state checks")
    @EnumSource(DoorState.class)
    void should_have_mutually_exclusive_fully_states(DoorState state) {
        // given
        var isFullyOpen = state.isFullyOpen();
        var isFullyClosed = state.isFullyClosed();

        // then
        assertThat(isFullyOpen && isFullyClosed).isFalse();
    }
}
