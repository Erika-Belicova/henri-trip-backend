package com.henritrip.controller;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserControllerTest {

    private static final String USERS_ENDPOINT = "/api/users";
    private static final String TEST_EMAIL = "user-management@example.com";
    private static final String TEST_PASSWORD = "Password1234+";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

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
        // persist a user for retrieval and deletion tests
        testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        testUser.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        testUser.setRole(UserRole.USER);

        testUser = userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    void tearDown() {
        // remove test users to keep executions independent
        userRepository.findByEmailIgnoreCase(TEST_EMAIL)
                .ifPresent(userRepository::delete);

        userRepository.findByEmailIgnoreCase("newuser@example.com")
                .ifPresent(userRepository::delete);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersShouldReturnUsersWhenAdminIsAuthenticated() throws Exception {
        // act & assert: verify that an administrator can retrieve all users
        mockMvc.perform(get(USERS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath(
                        "$[?(@.email == 'user-management@example.com')]"
                ).exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserByIdShouldReturnUserWhenUserExists() throws Exception {
        // act & assert: verify that an administrator can retrieve a user
        mockMvc.perform(get(USERS_ENDPOINT + "/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserByIdShouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        // act & assert: verify that an unknown user returns 404
        mockMvc.perform(get(USERS_ENDPOINT + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserShouldReturnNotFoundWhenUserDoesNotExist()
            throws Exception {

        // arrange: use an identifier that does not belong to an existing user
        long unknownUserId = 999999L;

        // act & assert: verify that deleting an unknown user returns 404
        mockMvc.perform(delete(USERS_ENDPOINT + "/" + unknownUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserShouldReturnCreatedWhenRequestIsValid() throws Exception {
        // arrange: create a valid user creation request
        String request = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "email", "newuser@example.com",
                        "password", TEST_PASSWORD,
                        "role", "USER"
                )
        );

        // act & assert: verify that the user is created successfully
        mockMvc.perform(post(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        // verify that the password is encoded before persistence
        User createdUser = userRepository
                .findByEmailIgnoreCase("newuser@example.com")
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches(TEST_PASSWORD, createdUser.getPasswordHash())
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserShouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        // arrange: provide a request using an existing email
        String request = objectMapper.writeValueAsString(
                java.util.Map.of(
                        "email", TEST_EMAIL,
                        "password", TEST_PASSWORD,
                        "role", "USER"
                )
        );

        // act & assert: verify that duplicate creation returns 409
        mockMvc.perform(post(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersShouldReturnForbiddenWhenRegularUserIsAuthenticated()
            throws Exception {
        // act & assert: verify that regular users cannot access administration
        mockMvc.perform(get(USERS_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsersShouldReturnUnauthorizedWhenUserIsNotAuthenticated()
            throws Exception {
        // act & assert: verify that unauthenticated requests are rejected
        mockMvc.perform(get(USERS_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserShouldReturnNoContentWhenUserExists() throws Exception {
        // act & assert: verify that an administrator can delete a user
        mockMvc.perform(delete(USERS_ENDPOINT + "/" + testUser.getId()))
                .andExpect(status().isNoContent());

        // verify that the user no longer exists
        org.junit.jupiter.api.Assertions.assertFalse(
                userRepository.existsById(testUser.getId())
        );
    }

    @Test
    void getCurrentUserShouldReturnAuthenticatedUser() throws Exception {
        // act & assert: retrieve the profile using the database ID in the JWT subject
        mockMvc.perform(get(USERS_ENDPOINT + "/me")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(testUser.getId().toString())
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void getCurrentUserShouldReturnUnauthorizedWhenNotAuthenticated()
            throws Exception {

        // act & assert: verify that unauthenticated requests are rejected
        mockMvc.perform(get(USERS_ENDPOINT + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUserShouldReturnNotFoundWhenUserDoesNotExist()
            throws Exception {

        // arrange: use a JWT subject that does not match an existing user
        String unknownUserId = "999999";

        // act & assert: verify that an unknown user returns 404
        mockMvc.perform(get(USERS_ENDPOINT + "/me")
                        .with(jwt().jwt(jwt -> jwt.subject(unknownUserId))))
                .andExpect(status().isNotFound());
    }

}
