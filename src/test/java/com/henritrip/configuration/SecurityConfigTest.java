package com.henritrip.configuration;

import com.henritrip.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityConfigTest {

    private static final String PROTECTED_ENDPOINT = "/actuator/info";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // provide test credentials for application configuration
        registry.add("JWT_SECRET",
                () -> "test-secret-key-for-henri-trip-security-tests-256-bits");

        registry.add("DB_USERNAME", () -> "test");
        registry.add("DB_PASSWORD", () -> "test");
    }

    @Test
    void shouldReturnUnauthorizedWhenRequestIsUnauthenticated() throws Exception {
        // arrange: create a request without authentication

        // act & assert: verify that access to a protected endpoint is rejected
        mockMvc.perform(get(PROTECTED_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsInvalid() throws Exception {
        // arrange: provide an invalid JWT as the bearer token

        // act & assert: verify that the invalid token is rejected
        mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessWhenJwtIsValid() throws Exception {
        // arrange: generate a valid JWT signed with the configured test secret
        String token = generateValidToken();

        // act & assert: verify that the authenticated request reaches the endpoint
        mockMvc.perform(get(PROTECTED_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    // generates a signed JWT containing the claims expected by the security configuration
    private String generateValidToken() {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("henri-trip")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .subject("1")
                .claim("role", "ROLE_USER")
                .build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        );

        return jwtEncoder.encode(parameters).getTokenValue();
    }

}
