package com.elevator.infrastructure.adapter.web;

import com.elevator.domain.exception.DomainException;
import com.elevator.domain.exception.ElevatorNotFoundException;
import com.elevator.domain.exception.InvalidFloorException;
import com.elevator.domain.exception.NoAvailableElevatorException;
import com.elevator.infrastructure.adapter.web.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ElevatorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleElevatorNotFound(ElevatorNotFoundException ex) {
        var correlationId = generateCorrelationId();
        log.warn("Elevator not found: {} [correlationId={}]", ex.getMessage(), correlationId);
        return ApiError.of(ex.getCode(), ex.getMessage(), correlationId);
    }

    @ExceptionHandler(InvalidFloorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidFloor(InvalidFloorException ex) {
        var correlationId = generateCorrelationId();
        log.warn("Invalid floor: {} [correlationId={}]", ex.getMessage(), correlationId);
        return ApiError.of(ex.getCode(), ex.getMessage(), correlationId);
    }

    @ExceptionHandler(NoAvailableElevatorException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleNoAvailableElevator(NoAvailableElevatorException ex) {
        var correlationId = generateCorrelationId();
        log.warn("No available elevator: {} [correlationId={}]", ex.getMessage(), correlationId);
        return ApiError.of(ex.getCode(), ex.getMessage(), correlationId);
    }

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDomainException(DomainException ex) {
        var correlationId = generateCorrelationId();
        log.warn("Domain exception: {} [correlationId={}]", ex.getMessage(), correlationId);
        return ApiError.of(ex.getCode(), ex.getMessage(), correlationId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException ex) {
        var correlationId = generateCorrelationId();
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {} [correlationId={}]", message, correlationId);
        return ApiError.of("VALIDATION_ERROR", message, correlationId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex) {
        var correlationId = generateCorrelationId();
        log.warn("Illegal argument: {} [correlationId={}]", ex.getMessage(), correlationId);
        return ApiError.of("INVALID_ARGUMENT", ex.getMessage(), correlationId);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception ex) {
        var correlationId = generateCorrelationId();
        log.error("Unexpected error [correlationId={}]", correlationId, ex);
        return ApiError.of("INTERNAL_ERROR", "An unexpected error occurred", correlationId);
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
