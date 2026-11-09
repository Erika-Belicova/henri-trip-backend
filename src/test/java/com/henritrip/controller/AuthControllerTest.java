package com.henritrip.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.User;
import com.henritrip.model.enums.UserRole;
import com.henritrip.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String TEST_EMAIL = "integration@example.com";
    private static final String TEST_PASSWORD = "Password1234+";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // provide test credentials for application configuration
        registry.add("JWT_SECRET",
                () -> "test-secret-key-for-henri-trip-security-tests-256-bits");

        registry.add("DB_USERNAME", () -> "test");
        registry.add("DB_PASSWORD", () -> "test");
    }

    @BeforeEach
    void setUp() {
        // persist a user with an encoded password for authentication tests
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        user.setRole(UserRole.USER);

        userRepository.saveAndFlush(user);
    }

    @AfterEach
    void tearDown() {
        // remove the test user to keep test executions independent
        userRepository.findByEmailIgnoreCase(TEST_EMAIL)
                .ifPresent(userRepository::delete);
    }

    @Test
    void loginShouldReturnAccessTokenAndAuthenticatedUser() throws Exception {
        // arrange: create a valid login request
        String request = objectMapper.writeValueAsString(
                Map.of(
                        "email", TEST_EMAIL,
                        "password", TEST_PASSWORD
                )
        );

        // act: authenticate and capture the response
        String response = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.user.updatedAt").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // assert: verify that the returned access token contains the expected claims
        JsonNode responseBody = objectMapper.readTree(response);
        String accessToken = responseBody.get("accessToken").asText();

        Jwt jwt = jwtDecoder.decode(accessToken);

        assertEquals("henri-trip", jwt.getClaimAsString("iss"));
        assertEquals(
                responseBody.get("user").get("id").asText(),
                jwt.getSubject()
        );
        assertEquals("ROLE_USER", jwt.getClaimAsString("role"));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenPasswordIsIncorrect() throws Exception {
        // arrange: provide an incorrect password
        String request = objectMapper.writeValueAsString(
                Map.of(
                        "email", TEST_EMAIL,
                        "password", "WrongPassword1234+"
                )
        );

        // act & assert: verify that authentication is rejected
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenUserDoesNotExist() throws Exception {
        // arrange: provide credentials for an unknown user
        String request = objectMapper.writeValueAsString(
                Map.of(
                        "email", "unknown@example.com",
                        "password", TEST_PASSWORD
                )
        );

        // act & assert: verify that authentication is rejected
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void loginShouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        // arrange: provide a request with a blank email
        String request = objectMapper.writeValueAsString(
                Map.of(
                        "email", "",
                        "password", TEST_PASSWORD
                )
        );

        // act & assert: verify that request validation rejects the email
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void loginShouldReturnBadRequestWhenEmailFormatIsInvalid() throws Exception {
        // arrange: provide an incorrectly formatted email
        String request = objectMapper.writeValueAsString(
                Map.of(
                        "email", "invalid-email",
                        "password", TEST_PASSWORD
                )
        );

        // act & assert: verify that email validation rejects the request
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

}
