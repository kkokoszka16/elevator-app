package com.elevator.infrastructure.adapter.web;

import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.exception.NoAvailableElevatorException;
import com.elevator.domain.model.ElevatorId;
import com.elevator.domain.model.Floor;
import com.elevator.infrastructure.adapter.web.dto.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    class ElevatorNotFoundHandling {

        @Test
        void should_return_api_error_with_correct_code() {
            // given
            var exception = new ElevatorNotFoundException(new ElevatorId(99));

            // when
            var result = handler.handleElevatorNotFound(exception);

            // then
            assertThat(result.code()).isEqualTo("ELEVATOR_NOT_FOUND");
            assertThat(result.message()).contains("99");
            assertThat(result.correlationId()).isNotBlank();
            assertThat(result.timestamp()).isNotNull();
        }
    }

    @Nested
    class InvalidFloorHandling {

        @Test
        void should_return_api_error_with_correct_code() {
            // given
            var exception = new InvalidFloorException(-5);

            // when
            var result = handler.handleInvalidFloor(exception);

            // then
            assertThat(result.code()).isEqualTo("INVALID_FLOOR");
            assertThat(result.message()).contains("-5");
            assertThat(result.correlationId()).isNotBlank();
        }
    }

    @Nested
    class NoAvailableElevatorHandling {

        @Test
        void should_return_api_error_with_correct_code() {
            // given
            var exception = new NoAvailableElevatorException(new Floor(5));

            // when
            var result = handler.handleNoAvailableElevator(exception);

            // then
            assertThat(result.code()).isEqualTo("NO_AVAILABLE_ELEVATOR");
            assertThat(result.message()).contains("5");
            assertThat(result.correlationId()).isNotBlank();
        }
    }

    @Nested
    class IllegalArgumentHandling {

        @Test
        void should_return_api_error_with_invalid_argument_code() {
            // given
            var exception = new IllegalArgumentException("Test error message");

            // when
            var result = handler.handleIllegalArgument(exception);

            // then
            assertThat(result.code()).isEqualTo("INVALID_ARGUMENT");
            assertThat(result.message()).isEqualTo("Test error message");
            assertThat(result.correlationId()).isNotBlank();
        }
    }

    @Nested
    class GenericExceptionHandling {

        @Test
        void should_return_internal_error() {
            // given
            var exception = new RuntimeException("Unexpected error");

            // when
            var result = handler.handleGenericException(exception);

            // then
            assertThat(result.code()).isEqualTo("INTERNAL_ERROR");
            assertThat(result.message()).isEqualTo("An unexpected error occurred");
            assertThat(result.correlationId()).isNotBlank();
        }

        @Test
        void should_not_expose_internal_details() {
            // given
            var exception = new RuntimeException("Database connection failed: password=secret");

            // when
            var result = handler.handleGenericException(exception);

            // then
            assertThat(result.message()).doesNotContain("Database");
            assertThat(result.message()).doesNotContain("secret");
        }
    }

    @Nested
    class CorrelationIdGeneration {

        @Test
        void should_generate_unique_correlation_ids() {
            // given
            var exception = new InvalidFloorException(-1);

            // when
            var result1 = handler.handleInvalidFloor(exception);
            var result2 = handler.handleInvalidFloor(exception);

            // then
            assertThat(result1.correlationId()).isNotEqualTo(result2.correlationId());
        }

        @Test
        void should_generate_8_character_correlation_id() {
            // given
            var exception = new InvalidFloorException(-1);

            // when
            var result = handler.handleInvalidFloor(exception);

            // then
            assertThat(result.correlationId()).hasSize(8);
        }
    }
}
