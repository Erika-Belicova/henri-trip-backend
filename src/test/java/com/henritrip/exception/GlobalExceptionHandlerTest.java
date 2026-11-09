package com.henritrip.exception;

import com.henritrip.dto.common.ApiErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        // provide a predictable request path for error responses
        when(request.getRequestURI()).thenReturn("/api/guides");
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws NoSuchMethodException {
        // arrange: simulate a validation failure on the guide title field
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "guide");

        bindingResult.addError(
                new FieldError("guide", "title", "Title must not be blank")
        );

        // Spring requires a MethodParameter when constructing this exception
        // the helper method below provides a minimal method parameter for the test
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        new MethodParameter(
                                getClass().getDeclaredMethod("testMethod", String.class),
                                0
                        ),
                        bindingResult
                );

        // act: pass the validation exception to the global exception handler
        ResponseEntity<@NonNull ApiErrorResponseDTO> response =
                handler.handleValidationException(exception, request);

        // assert: verify the HTTP status and the field-specific validation
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals(
                "Title must not be blank",
                response.getBody().getValidationErrors().get("title")
        );
    }

    // minimal helper method used to obtain the MethodParameter required by Spring
    private void testMethod(String value) {}

    @Test
    void shouldReturnUnauthorizedForBadCredentials() {
        // arrange: simulate failed authentication due to invalid credentials
        BadCredentialsException exception =
                new BadCredentialsException("Invalid credentials");

        // act: handle the authentication exception
        ResponseEntity<@NonNull ApiErrorResponseDTO> response =
                handler.handleBadCredentials(exception, request);

        // assert: verify that the response indicates unauthorized access
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid email or password",
                response.getBody().getMessage());
    }

    @Test
    void shouldReturnNotFoundForMissingUser() {
        // arrange: simulate a user lookup failure
        UserNotFoundException exception =
                new UserNotFoundException("User not found");

        // act: handle the user-not-found exception
        ResponseEntity<@NonNull ApiErrorResponseDTO> response =
                handler.handleUserNotFound(exception, request);

        // assert: verify that the response indicates the requested user was not found
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() {
        // arrange: simulate an unexpected application error
        RuntimeException exception =
                new RuntimeException("Internal database error");

        // act: handle the unexpected exception
        ResponseEntity<@NonNull ApiErrorResponseDTO> response =
                handler.handleGeneralException(exception, request);

        // assert: verify that internal details are not exposed in the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred",
                response.getBody().getMessage());
    }

}