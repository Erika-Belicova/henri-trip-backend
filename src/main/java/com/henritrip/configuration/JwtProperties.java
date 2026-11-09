package com.henritrip.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT handling.
 * Binds properties prefixed with "jwt" from application.properties.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    @NotBlank
    @Size(min = 32)
    private String secretKey;

    @Min(1)
    private long accessTokenDuration;

}
