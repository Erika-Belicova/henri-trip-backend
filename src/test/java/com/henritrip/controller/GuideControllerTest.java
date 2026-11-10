package com.henritrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.*;
import com.henritrip.model.enums.*;
import com.henritrip.repository.GuideRepository;
import com.henritrip.repository.UserRepository;
import com.henritrip.repository.GuideInvitationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class GuideControllerTest {

    private static final String GUIDES_ENDPOINT = "/api/guides";
    private static final String ADMIN_EMAIL = "guide-admin@example.com";
    private static final String USER_EMAIL = "guide-user@example.com";
    private static final String OTHER_USER_EMAIL = "guide-other@example.com";
    private static final String TEST_PASSWORD = "Password1234+";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuideInvitationRepository guideInvitationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User user;
    private User otherUser;

    private Guide invitedGuide;
    private Guide uninvitedGuide;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("JWT_SECRET",
                () -> "test-secret-key-for-henri-trip-security-tests-256-bits");

        registry.add("DB_USERNAME", () -> "test");
        registry.add("DB_PASSWORD", () -> "test");
    }

    @BeforeEach
    void setUp() {
        admin = createUser(ADMIN_EMAIL, UserRole.ADMIN);
        user = createUser(USER_EMAIL, UserRole.USER);
        otherUser = createUser(OTHER_USER_EMAIL, UserRole.USER);

        invitedGuide = createGuide("Invited Guide", admin);
        uninvitedGuide = createGuide("Uninvited Guide", admin);

        createInvitation(invitedGuide, user, admin);
    }

    @AfterEach
    void tearDown() {
        // remove invitations before deleting their associated entities
        guideInvitationRepository.deleteAll();

        guideRepository.deleteAll();

        userRepository.findByEmailIgnoreCase(ADMIN_EMAIL)
                .ifPresent(userRepository::delete);

        userRepository.findByEmailIgnoreCase(USER_EMAIL)
                .ifPresent(userRepository::delete);

        userRepository.findByEmailIgnoreCase(OTHER_USER_EMAIL)
                .ifPresent(userRepository::delete);
    }

    private User createUser(String email, UserRole role) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        newUser.setRole(role);

        return userRepository.saveAndFlush(newUser);
    }

    private Guide createGuide(String title, User creator) {
        Guide guide = new Guide();
        guide.setTitle(title);
        guide.setDescription("A test travel guide");
        guide.setCreatedBy(creator);
        guide.setTransports(Set.of(TransportOption.WALKING));
        guide.setSeasons(Set.of(SeasonOption.SUMMER));
        guide.setAudiences(Set.of(AudienceOption.FRIENDS));

        GuideDay day = new GuideDay();
        day.setDayNumber(1);
        guide.addDay(day);

        return guideRepository.saveAndFlush(guide);
    }

    private void createInvitation(Guide guide, User invitedUser, User invitedBy) {
        GuideInvitation invitation = new GuideInvitation();

        invitation.setId(new GuideInvitationId(
                guide.getId(),
                invitedUser.getId()
        ));

        invitation.setGuide(guide);
        invitation.setUser(invitedUser);
        invitation.setInvitedBy(invitedBy);

        guideInvitationRepository.saveAndFlush(invitation);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor
    authenticatedAs(User authenticatedUser) {
        return jwt().jwt(jwt -> jwt
                .subject(authenticatedUser.getId().toString())
        ).authorities(
                new SimpleGrantedAuthority(
                        "ROLE_" + authenticatedUser.getRole().name()
                )
        );
    }

    @Test
    void getAllGuidesShouldReturnAllGuidesWhenAdminIsAuthenticated()
            throws Exception {

        mockMvc.perform(get(GUIDES_ENDPOINT)
                        .with(authenticatedAs(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath(
                        "$.content[?(@.title == 'Invited Guide')]"
                ).exists())
                .andExpect(jsonPath(
                        "$.content[?(@.title == 'Uninvited Guide')]"
                ).exists());
    }

    @Test
    void getAllGuidesShouldReturnOnlyInvitedGuidesWhenUserIsAuthenticated()
            throws Exception {

        mockMvc.perform(get(GUIDES_ENDPOINT)
                        .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath(
                        "$.content[?(@.title == 'Invited Guide')]"
                ).exists())
                .andExpect(jsonPath(
                        "$.content[?(@.title == 'Uninvited Guide')]"
                ).doesNotExist());
    }

    @Test
    void getGuideByIdShouldReturnGuideWhenUserIsInvited()
            throws Exception {

        mockMvc.perform(get(GUIDES_ENDPOINT + "/" + invitedGuide.getId())
                        .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invitedGuide.getId()))
                .andExpect(jsonPath("$.title").value("Invited Guide"))
                .andExpect(jsonPath("$.description")
                        .value("A test travel guide"))
                .andExpect(jsonPath("$.days[0].dayNumber").value(1));
    }

    @Test
    void getGuideByIdShouldReturnGuideWhenAdminIsAuthenticated()
            throws Exception {

        mockMvc.perform(get(GUIDES_ENDPOINT + "/" + uninvitedGuide.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Uninvited Guide"));
    }

    @Test
    void getGuideByIdShouldReturnNotFoundWhenUserIsNotInvited()
            throws Exception {

        mockMvc.perform(get(GUIDES_ENDPOINT + "/" + uninvitedGuide.getId())
                        .with(authenticatedAs(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGuideByIdShouldReturnNotFoundWhenGuideDoesNotExist()
            throws Exception {

        mockMvc.perform(get(GUIDES_ENDPOINT + "/999999")
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllGuidesShouldReturnUnauthorizedWhenNotAuthenticated()
            throws Exception {

        mockMvc.perform(get(GUIDES_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createGuideShouldReturnCreatedWhenAdminIsAuthenticated()
            throws Exception {

        Map<String, Object> request = Map.of(
                "title", "New Test Guide",
                "description", "A newly created travel guide",
                "numberOfDays", 1,
                "transports", List.of("CAR"),
                "seasons", List.of("SUMMER"),
                "audiences", List.of("FAMILY")
        );

        mockMvc.perform(post(GUIDES_ENDPOINT)
                        .with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("New Test Guide"))
                .andExpect(jsonPath("$.days[0].dayNumber").value(1));

        org.junit.jupiter.api.Assertions.assertTrue(
                guideRepository.findAll().stream()
                        .anyMatch(guide ->
                                guide.getTitle().equals("New Test Guide"))
        );
    }

    @Test
    void createGuideShouldReturnForbiddenWhenRegularUserIsAuthenticated()
            throws Exception {

        Map<String, Object> request = Map.of(
                "title", "Unauthorized Guide",
                "description", "Should not be created",
                "numberOfDays", 1,
                "transports", List.of("CAR"),
                "seasons", List.of("SUMMER"),
                "audiences", List.of("FAMILY")
        );

        mockMvc.perform(post(GUIDES_ENDPOINT)
                        .with(authenticatedAs(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createGuideShouldReturnBadRequestWhenTitleIsBlank()
            throws Exception {

        Map<String, Object> request = Map.of(
                "title", "",
                "description", "A test guide",
                "transports", List.of("CAR"),
                "seasons", List.of("SUMMER"),
                "audiences", List.of("FAMILY"),
                "days", List.of(
                        Map.of("dayNumber", 1, "activities", List.of())
                )
        );

        mockMvc.perform(post(GUIDES_ENDPOINT)
                        .with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    void createGuideShouldReturnUnauthorizedWhenNotAuthenticated()
            throws Exception {

        mockMvc.perform(post(GUIDES_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateGuideShouldReturnOkWhenAdminIsAuthenticated()
            throws Exception {

        Map<String, Object> request = Map.of(
                "title", "Updated Guide",
                "description", "Updated description",
                "numberOfDays", 2,
                "transports", List.of("BICYCLE"),
                "seasons", List.of("SPRING"),
                "audiences", List.of("GROUP")
        );

        mockMvc.perform(put(GUIDES_ENDPOINT + "/" + invitedGuide.getId())
                        .with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Guide"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"));
    }

    @Test
    void updateGuideShouldReturnForbiddenWhenRegularUserIsAuthenticated()
            throws Exception {

        Map<String, Object> request = Map.of(
                "title", "Unauthorized Update",
                "description", "Should not be updated",
                "numberOfDays", 1,
                "transports", List.of("CAR"),
                "seasons", List.of("SUMMER"),
                "audiences", List.of("FAMILY")
        );

        mockMvc.perform(put(GUIDES_ENDPOINT + "/" + invitedGuide.getId())
                        .with(authenticatedAs(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateGuideShouldReturnNotFoundWhenGuideDoesNotExist()
            throws Exception {

        Map<String, Object> request = Map.of(
                "title", "Updated Guide",
                "description", "Updated description",
                "numberOfDays", 1,
                "transports", List.of("CAR"),
                "seasons", List.of("SUMMER"),
                "audiences", List.of("FAMILY")
        );

        mockMvc.perform(put(GUIDES_ENDPOINT + "/999999")
                        .with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateGuideShouldReturnBadRequestWhenDescriptionIsBlank()
            throws Exception {

        Map<String, Object> request = Map.of(
                "title", "Updated Guide",
                "description", "",
                "transports", List.of("CAR"),
                "seasons", List.of("SUMMER"),
                "audiences", List.of("FAMILY")
        );

        mockMvc.perform(put(GUIDES_ENDPOINT + "/" + invitedGuide.getId())
                        .with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.description").exists());
    }

    @Test
    void deleteGuideShouldReturnNoContentWhenAdminIsAuthenticated()
            throws Exception {

        Long guideId = uninvitedGuide.getId();

        mockMvc.perform(delete(GUIDES_ENDPOINT + "/" + guideId)
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                guideRepository.existsById(guideId)
        );
    }

    @Test
    void deleteGuideShouldReturnForbiddenWhenRegularUserIsAuthenticated()
            throws Exception {

        mockMvc.perform(delete(GUIDES_ENDPOINT + "/" + invitedGuide.getId())
                        .with(authenticatedAs(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteGuideShouldReturnNotFoundWhenGuideDoesNotExist()
            throws Exception {

        mockMvc.perform(delete(GUIDES_ENDPOINT + "/999999")
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGuideShouldReturnUnauthorizedWhenNotAuthenticated()
            throws Exception {

        mockMvc.perform(delete(GUIDES_ENDPOINT + "/" + invitedGuide.getId()))
                .andExpect(status().isUnauthorized());
    }

}