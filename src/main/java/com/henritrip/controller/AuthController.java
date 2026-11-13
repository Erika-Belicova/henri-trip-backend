package com.henritrip.controller;

import com.henritrip.dto.authentication.AuthenticationResponseDTO;
import com.henritrip.dto.authentication.LoginRequestDTO;
import com.henritrip.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for handling user authentication.
 * Exposes an endpoint for user login.
 */
@Tag(
        name = "Authentication",
        description = "User authentication endpoints"
)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns an access token and user information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User logged in successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<@NonNull AuthenticationResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO loginRequest
    ) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

}
