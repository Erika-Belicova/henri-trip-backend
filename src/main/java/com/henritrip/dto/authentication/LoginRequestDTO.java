package com.henritrip.dto.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for authentication requests.
 * Requires a valid email address and password.
 */
@Data
public class LoginRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address of the user", example = "jean@martin.fr")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Schema(description = "Password of the user", example = "mot-de-passe99")
    private String password;

}
