package com.henritrip.dto.user;

import com.henritrip.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for creating a new user or administrator.
 */
@Data
public class CreateUserRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Size(max = 320, message = "Email cannot exceed 320 characters")
    @Schema(description = "Email address of the new user", example = "jean@martin.fr")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Schema(description = "Password of the new user", example = "mot-de-passe16")
    private String password;

    @NotNull(message = "User role is required")
    @Schema(description = "Role assigned to the new user", example = "USER")
    private UserRole role;

}
