package com.henritrip.controller;

import com.henritrip.dto.user.CreateUserRequestDTO;
import com.henritrip.dto.user.UserResponseDTO;
import com.henritrip.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for administrative user management.
 * Exposes endpoints for retrieving, creating and deleting users.
 */
@Tag(
        name = "Users",
        description = "Administrative user management endpoints"
)
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Retrieve the authenticated user",
            description = "Returns the account information of the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public ResponseEntity<@NonNull UserResponseDTO> getCurrentUser(
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @Operation(
            summary = "Retrieve all users",
            description = "Returns information about all registered users."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<@NonNull List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
            summary = "Retrieve a user",
            description = "Returns information about a user identified by their ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<@NonNull UserResponseDTO> getUserById(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @Operation(
            summary = "Create a user",
            description = "Creates a new user or administrator account."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<@NonNull UserResponseDTO> createUser(
            @RequestBody @Valid CreateUserRequestDTO requestDTO
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(requestDTO));
    }

    @Operation(
            summary = "Delete a user",
            description = "Deletes a user identified by their ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<@NonNull Void> deleteUser(
            @PathVariable Long userId
    ) {
        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

}