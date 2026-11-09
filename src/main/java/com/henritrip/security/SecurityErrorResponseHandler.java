package com.henritrip.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henritrip.dto.common.ApiErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Handles authentication and authorization errors occurring
 * within the Spring Security filter chain.
 */
@Component
public class SecurityErrorResponseHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // handles unauthenticated requests or requests with invalid authentication tokens
    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException exception
    ) throws IOException {

        writeErrorResponse(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "Authentication required or token is invalid"
        );
    }

    // handles authenticated users attempting to access resources they are not authorized to use
    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException exception
    ) throws IOException {

        writeErrorResponse(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource"
        );
    }

    // creates and writes a standardized JSON error response directly to the HTTP response
    private void writeErrorResponse(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull HttpStatus status,
            @NonNull String message
    ) throws IOException {

        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO();

        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(message);
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setTimestamp(OffsetDateTime.now());

        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
