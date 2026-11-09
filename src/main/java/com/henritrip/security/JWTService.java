package com.henritrip.security;

import com.henritrip.configuration.JwtProperties;
import com.henritrip.exception.JwtGenerationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JWTService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JWTService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Instant now = Instant.now();

        // define the token's standard claims and application-specific role
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("henri-trip")
                .issuedAt(now)
                .expiresAt(now.plusMillis(
                        jwtProperties.getAccessTokenDuration()
                ))
                .subject(userDetails.getId().toString())
                .claim("role", userDetails.getRole())
                .build();

        // sign the token using the configured JWT encoder and HS256 algorithm
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        );

        try {
            return jwtEncoder.encode(parameters).getTokenValue();
        } catch (JwtEncodingException exception) {
            throw new JwtGenerationException(
                    "Token generation failed."
            );
        }
    }

}