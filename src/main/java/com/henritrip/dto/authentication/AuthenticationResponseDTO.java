package com.henritrip.dto.authentication;

import com.henritrip.dto.user.UserResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO returned after successful authentication.
 * Contains the JWT access token and authenticated user's information.
 */
@Data
public class AuthenticationResponseDTO {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Authenticated user's information")
    private UserResponseDTO user;

}
