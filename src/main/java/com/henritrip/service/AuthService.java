package com.henritrip.service;

import com.henritrip.dto.authentication.AuthenticationResponseDTO;
import com.henritrip.dto.authentication.LoginRequestDTO;
import com.henritrip.dto.user.UserResponseDTO;
import com.henritrip.security.CustomUserDetails;
import com.henritrip.security.JWTService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service responsible for handling user authentication.
 * Validates credentials and returns JWT access tokens.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JWTService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthenticationResponseDTO login(LoginRequestDTO loginRequest) {

        // authenticate user with provided login credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail().trim(),
                        loginRequest.getPassword()
                )
        );

        // ensure the authenticated principal is a valid application user
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException(
                    "Authenticated principal is not a valid user"
            );
        }

        // generate an access token for the authenticated user
        String accessToken = jwtService.generateAccessToken(authentication);

        // return the access token and authenticated user's information
        AuthenticationResponseDTO response = new AuthenticationResponseDTO();
        response.setAccessToken(accessToken);
        response.setUser(toUserResponseDTO(userDetails));

        return response;
    }

    // converts authenticated user details to a UserResponseDTO
    private UserResponseDTO toUserResponseDTO(CustomUserDetails userDetails) {
        UserResponseDTO response = new UserResponseDTO();

        response.setId(userDetails.getId());
        response.setEmail(userDetails.getUsername());
        response.setRole(userDetails.getUserRole());
        response.setCreatedAt(userDetails.getCreatedAt());
        response.setUpdatedAt(userDetails.getUpdatedAt());

        return response;
    }

}
