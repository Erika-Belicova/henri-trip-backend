package com.henritrip.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI documentation.
 * Defines API metadata, server information, and the JWT Bearer
 * authentication scheme used by the API.
 */
@Configuration
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenAPIConfig {

    @Bean
    public OpenAPI defineOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HenriTrip API")
                        .version("1.0.0")
                        .description("REST API for the HenriTrip application."));
    }

}
