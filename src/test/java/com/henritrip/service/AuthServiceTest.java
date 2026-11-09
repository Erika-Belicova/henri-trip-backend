package com.henritrip.service;

import com.henritrip.dto.authentication.AuthenticationResponseDTO;
import com.henritrip.dto.authentication.LoginRequestDTO;
import com.henritrip.exception.JwtGenerationException;
import com.henritrip.model.entity.User;
import com.henritrip.model.enums.UserRole;
import com.henritrip.security.CustomUserDetails;
import com.henritrip.security.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;
    private AuthenticationManager authenticationManager;
    private JWTService jwtService;
    private Authentication authentication;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JWTService.class);
        authentication = mock(Authentication.class);

        // configure a predictable authenticated user
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("encoded-password");
        user.setRole(UserRole.USER);

        OffsetDateTime now = OffsetDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userDetails = new CustomUserDetails(user);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        authService = new AuthService(authenticationManager, jwtService);
    }

    @Test
    void loginShouldReturnAuthenticationResponseWhenCredentialsAreValid() {
        // arrange: configure successful authentication and JWT generation
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(" user@example.com ");
        loginRequest.setPassword("password123");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        when(jwtService.generateAccessToken(authentication))
                .thenReturn("generated-access-token");

        // capture the authentication request for verification
        ArgumentCaptor<Authentication> captor =
                ArgumentCaptor.forClass(Authentication.class);

        // act: authenticate the user
        AuthenticationResponseDTO response = authService.login(loginRequest);

        // assert: verify the returned authentication response
        assertNotNull(response);
        assertEquals("generated-access-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());

        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("user@example.com", response.getUser().getEmail());
        assertEquals(UserRole.USER, response.getUser().getRole());
        assertEquals(userDetails.getCreatedAt(), response.getUser().getCreatedAt());
        assertEquals(userDetails.getUpdatedAt(), response.getUser().getUpdatedAt());

        // verify that credentials were normalized and authenticated
        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken authenticationRequest =
                (UsernamePasswordAuthenticationToken) captor.getValue();

        assertEquals("user@example.com", authenticationRequest.getPrincipal());
        assertEquals("password123", authenticationRequest.getCredentials());

        // verify that an access token was generated for the authenticated user
        verify(jwtService).generateAccessToken(authentication);
    }

    @Test
    void loginShouldPropagateExceptionWhenCredentialsAreInvalid() {
        // arrange: configure authentication to reject the provided credentials
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("wrong-password");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // act & assert: verify that the authentication failure is propagated
        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        // verify that no JWT was generated after authentication failed
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginShouldThrowExceptionWhenAuthenticatedPrincipalIsInvalid() {
        // arrange: configure authentication with an unexpected principal
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password123");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn("unexpected-principal");

        // act & assert: verify that the unexpected principal is rejected
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals(
                "Authenticated principal is not a valid user",
                exception.getMessage()
        );

        // verify that no JWT was generated for an invalid principal
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginShouldPropagateExceptionWhenTokenGenerationFails() {
        // arrange: configure successful authentication but failed JWT generation
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password123");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        when(jwtService.generateAccessToken(authentication))
                .thenThrow(new JwtGenerationException("Token generation failed."));

        // act & assert: verify that the JWT generation failure is propagated
        JwtGenerationException exception = assertThrows(
                JwtGenerationException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Token generation failed.", exception.getMessage());

        // verify that authentication was successful before token generation failed
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtService).generateAccessToken(authentication);
    }

}
