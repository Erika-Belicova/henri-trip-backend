package com.henritrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideDay;
import com.henritrip.model.entity.User;
import com.henritrip.model.enums.*;
import com.henritrip.repository.GuideInvitationRepository;
import com.henritrip.repository.GuideRepository;
import com.henritrip.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class GuideInvitationControllerTest {

    private static final String GUIDES_ENDPOINT = "/api/guides";
    private static final String ADMIN_EMAIL = "invitation-admin@example.com";
    private static final String USER_EMAIL = "invitation-user@example.com";
    private static final String OTHER_USER_EMAIL = "invitation-other@example.com";
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

    private Guide guide;

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

        guide = createGuide("Paris Travel Guide", admin);
    }

    @AfterEach
    void tearDown() {
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
        Guide newGuide = new Guide();
        newGuide.setTitle(title);
        newGuide.setDescription("A test travel guide");
        newGuide.setCreatedBy(creator);
        newGuide.setTransports(java.util.Set.of(TransportOption.WALKING));
        newGuide.setSeasons(java.util.Set.of(SeasonOption.SUMMER));
        newGuide.setAudiences(java.util.Set.of(AudienceOption.FRIENDS));

        GuideDay day = new GuideDay();
        day.setDayNumber(1);

        newGuide.addDay(day);

        return guideRepository.saveAndFlush(newGuide);
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

    private String invitationEndpoint() {
        return GUIDES_ENDPOINT + "/" + guide.getId() + "/invitations";
    }

    // post : invite user

    @Test
    void inviteUserShouldReturnCreatedWhenAdminIsAuthenticated()
            throws Exception {

        mockMvc.perform(post(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guideId").value(guide.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.userEmail").value(USER_EMAIL))
                .andExpect(jsonPath("$.invitedById").value(admin.getId()));

        org.junit.jupiter.api.Assertions.assertTrue(
                guideInvitationRepository.existsByIdGuideIdAndIdUserId(
                        guide.getId(),
                        user.getId()
                )
        );
    }

    @Test
    void inviteUserShouldReturnConflictWhenUserIsAlreadyInvited()
            throws Exception {

        mockMvc.perform(post(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isConflict());
    }

    @Test
    void inviteUserShouldReturnNotFoundWhenGuideDoesNotExist()
            throws Exception {

        mockMvc.perform(post(
                        GUIDES_ENDPOINT + "/999999/invitations/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void inviteUserShouldReturnNotFoundWhenUserDoesNotExist()
            throws Exception {

        mockMvc.perform(post(invitationEndpoint() + "/999999")
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void inviteUserShouldReturnForbiddenWhenRegularUserIsAuthenticated()
            throws Exception {

        mockMvc.perform(post(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void inviteUserShouldReturnUnauthorizedWhenNotAuthenticated()
            throws Exception {

        mockMvc.perform(post(invitationEndpoint() + "/" + user.getId()))
                .andExpect(status().isUnauthorized());
    }

    // get : retrieve guide invitations

    @Test
    void getGuideInvitationsShouldReturnAllInvitations()
            throws Exception {

        mockMvc.perform(post(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(invitationEndpoint() + "/" + otherUser.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(invitationEndpoint())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getGuideInvitationsShouldReturnEmptyListWhenNoUsersAreInvited()
            throws Exception {

        mockMvc.perform(get(invitationEndpoint())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getGuideInvitationsShouldReturnNotFoundWhenGuideDoesNotExist()
            throws Exception {

        mockMvc.perform(get(GUIDES_ENDPOINT + "/999999/invitations")
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGuideInvitationsShouldReturnForbiddenWhenRegularUserIsAuthenticated()
            throws Exception {

        mockMvc.perform(get(invitationEndpoint())
                        .with(authenticatedAs(user)))
                .andExpect(status().isForbidden());
    }

    // delete : remove guide invitation

    @Test
    void removeInvitationShouldReturnNoContentWhenAdminIsAuthenticated()
            throws Exception {

        mockMvc.perform(post(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                guideInvitationRepository.existsByIdGuideIdAndIdUserId(
                        guide.getId(),
                        user.getId()
                )
        );
    }

    @Test
    void removeInvitationShouldReturnForbiddenWhenRegularUserIsAuthenticated()
            throws Exception {

        mockMvc.perform(delete(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeInvitationShouldReturnNotFoundWhenGuideDoesNotExist()
            throws Exception {

        mockMvc.perform(delete(
                        GUIDES_ENDPOINT + "/999999/invitations/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNotFound());
    }

    // invitation access life cycle

    @Test
    void invitationShouldGrantAndRevocationShouldRemoveGuideAccess()
            throws Exception {

        // initially, the user cannot access the guide
        mockMvc.perform(get(GUIDES_ENDPOINT + "/" + guide.getId())
                        .with(authenticatedAs(user)))
                .andExpect(status().isNotFound());

        // admin invites the user
        mockMvc.perform(post(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isCreated());

        // the invited user can now access the guide
        mockMvc.perform(get(GUIDES_ENDPOINT + "/" + guide.getId())
                        .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guide.getId()))
                .andExpect(jsonPath("$.title").value("Paris Travel Guide"));

        // admin revokes the invitation
        mockMvc.perform(delete(invitationEndpoint() + "/" + user.getId())
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNoContent());

        // the user can no longer access the guide
        mockMvc.perform(get(GUIDES_ENDPOINT + "/" + guide.getId())
                        .with(authenticatedAs(user)))
                .andExpect(status().isNotFound());
    }

}
