package com.henritrip.dto.user;

import com.henritrip.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * DTO representing a user.
 * Exposes account information while excluding passwords and other authentication secrets.
 */
@Data
public class UserResponseDTO {

    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Schema(description = "Email address of the user", example = "jean@martin.fr")
    private String email;

    @Schema(description = "Role assigned to the user", example = "USER")
    private UserRole role;

    @Schema(description = "Date and time the user was created")
    private OffsetDateTime createdAt;

    @Schema(description = "Date and time the user was last updated")
    private OffsetDateTime updatedAt;

}
