package com.henritrip.exception;

import com.henritrip.dto.common.ApiErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // handles request validation failures and extracts field-level validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ApiErrorResponseDTO> handleValidationException(
            @NonNull MethodArgumentNotValidException exception,
            @NonNull HttpServletRequest request) {

        Map<String, String> validationErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Invalid value",
                        (existing, replacement) -> existing
                ));

        logger.debug("Request validation failed: {}", validationErrors);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "One or more validation errors occurred",
                request,
                validationErrors
        );
    }

    // handles failed authentication attempts
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<@NonNull ApiErrorResponseDTO> handleBadCredentials(
            @NonNull BadCredentialsException exception,
            @NonNull HttpServletRequest request) {

        logger.debug("Authentication failed");

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                request
        );
    }

    // handles requests referencing users that cannot be found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<@NonNull ApiErrorResponseDTO> handleUserNotFound(
            @NonNull UserNotFoundException exception,
            @NonNull HttpServletRequest request) {

        logger.debug("User not found: {}", exception.getMessage());

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    // handles unexpected application exceptions without exposing internal details
    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull ApiErrorResponseDTO> handleGeneralException(
            @NonNull Exception exception,
            @NonNull HttpServletRequest request) {

        logger.error("Unexpected application error", exception);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    // builds a standardized error response without validation errors
    private ResponseEntity<@NonNull ApiErrorResponseDTO> buildErrorResponse(
            @NonNull HttpStatus status,
            @NonNull String message,
            @NonNull HttpServletRequest request) {

        return buildErrorResponse(status, message, request, null);
    }

    // builds a standardized error response with optional field-level validation errors
    private ResponseEntity<@NonNull ApiErrorResponseDTO> buildErrorResponse(
            @NonNull HttpStatus status,
            @NonNull String message,
            @NonNull HttpServletRequest request,
            Map<String, String> validationErrors) {

        ApiErrorResponseDTO response = new ApiErrorResponseDTO();

        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setMessage(message);
        response.setPath(request.getRequestURI());
        response.setTimestamp(OffsetDateTime.now());
        response.setValidationErrors(validationErrors);

        return ResponseEntity.status(status).body(response);
    }

}
