package com.henritrip.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henritrip.dto.common.ApiErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;

class SecurityErrorResponseHandlerTest {

    private SecurityErrorResponseHandler handler;
    private ObjectMapper objectMapper;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules();
        handler = new SecurityErrorResponseHandler(objectMapper);

        request = new MockHttpServletRequest();
        request.setRequestURI("/api/guides");

        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthenticationFails()
            throws Exception {

        handler.commence(
                request,
                response,
                new BadCredentialsException("Invalid token")
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                response.getStatus()
        );

        assertTrue(
                response.getContentType().startsWith(
                        MediaType.APPLICATION_JSON_VALUE
                )
        );

        ApiErrorResponseDTO body = objectMapper.readValue(
                response.getContentAsString(),
                ApiErrorResponseDTO.class
        );

        assertEquals(401, body.getStatus());
        assertEquals(
                "Authentication required or token is invalid",
                body.getMessage()
        );
        assertEquals("/api/guides", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void shouldReturnForbiddenWhenAccessIsDenied()
            throws Exception {

        handler.handle(
                request,
                response,
                new AccessDeniedException("Access denied")
        );

        assertEquals(
                HttpStatus.FORBIDDEN.value(),
                response.getStatus()
        );

        assertTrue(
                response.getContentType().startsWith(
                        MediaType.APPLICATION_JSON_VALUE
                )
        );

        ApiErrorResponseDTO body = objectMapper.readValue(
                response.getContentAsString(),
                ApiErrorResponseDTO.class
        );

        assertEquals(403, body.getStatus());
        assertEquals(
                "You do not have permission to access this resource",
                body.getMessage()
        );
        assertEquals("/api/guides", body.getPath());
        assertNotNull(body.getTimestamp());
    }
}
