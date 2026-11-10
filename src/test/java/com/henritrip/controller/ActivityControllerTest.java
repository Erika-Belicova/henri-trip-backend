package com.henritrip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideDay;
import com.henritrip.model.entity.User;
import com.henritrip.model.enums.*;
import com.henritrip.repository.ActivityRepository;
import com.henritrip.repository.GuideRepository;
import com.henritrip.repository.UserRepository;
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

import java.util.Map;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ActivityControllerTest {

    private static final String ADMIN_EMAIL = "activity-admin@example.com";
    private static final String USER_EMAIL = "activity-user@example.com";
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
    private ActivityRepository activityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User user;

    private Guide guide;
    private GuideDay firstDay;
    private GuideDay secondDay;

    private Long activityId;

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

        guide = createGuide();

        firstDay = guide.getDays().get(0);
        secondDay = guide.getDays().get(1);

        activityId = createActivity(firstDay);
    }

    @AfterEach
    void tearDown() {
        activityRepository.deleteAll();
        guideRepository.deleteAll();

        userRepository.findByEmailIgnoreCase(ADMIN_EMAIL)
                .ifPresent(userRepository::delete);

        userRepository.findByEmailIgnoreCase(USER_EMAIL)
                .ifPresent(userRepository::delete);
    }

    private User createUser(String email, UserRole role) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        newUser.setRole(role);

        return userRepository.saveAndFlush(newUser);
    }

    private Guide createGuide() {
        Guide newGuide = new Guide();
        newGuide.setTitle("Paris Travel Guide");
        newGuide.setDescription("A test travel guide");
        newGuide.setCreatedBy(admin);
        newGuide.setTransports(Set.of(TransportOption.WALKING));
        newGuide.setSeasons(Set.of(SeasonOption.SUMMER));
        newGuide.setAudiences(Set.of(AudienceOption.FRIENDS));

        GuideDay dayOne = new GuideDay();
        dayOne.setDayNumber(1);

        GuideDay dayTwo = new GuideDay();
        dayTwo.setDayNumber(2);

        newGuide.addDay(dayOne);
        newGuide.addDay(dayTwo);

        return guideRepository.saveAndFlush(newGuide);
    }

    private Long createActivity(GuideDay day) {
        return mockCreateActivity(day);
    }

    private Long mockCreateActivity(GuideDay day) {
        // Create the initial activity through the API.
        try {
            String response = mockMvc.perform(post(activitiesEndpoint(day))
                            .with(authenticatedAs(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    validActivityRequest(1)
                            )))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            return objectMapper.readTree(response)
                    .get("id").asLong();

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Map<String, Object> validActivityRequest(int visitOrder) {
        return Map.of(
                "title", "Visit the Louvre Museum",
                "description", "Discover the famous art museum",
                "category", ActivityCategory.MUSEUM.name(),
                "address", "Rue de Rivoli, Paris, France",
                "visitOrder", visitOrder
        );
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

    private String activitiesEndpoint(GuideDay day) {
        return "/api/guides/" + guide.getId()
                + "/days/" + day.getId() + "/activities";
    }

    // get activities

    @Test
    void getActivitiesShouldReturnActivities() throws Exception {
        mockMvc.perform(get(activitiesEndpoint(firstDay))
                        .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title")
                        .value("Visit the Louvre Museum"));
    }

    // get activity

    @Test
    void getActivityShouldReturnActivity() throws Exception {
        mockMvc.perform(get(
                        activitiesEndpoint(firstDay) + "/" + activityId)
                        .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.title")
                        .value("Visit the Louvre Museum"));
    }

    // create activity

    @Test
    void createActivityShouldReturnCreatedWhenAdminIsAuthenticated()
            throws Exception {

        mockMvc.perform(post(activitiesEndpoint(firstDay))
                        .with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validActivityRequest(2)
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title")
                        .value("Visit the Louvre Museum"))
                .andExpect(jsonPath("$.visitOrder").value(2));
    }

    @Test
    void createActivityShouldReturnForbiddenWhenRegularUserIsAuthenticated()
            throws Exception {

        mockMvc.perform(post(activitiesEndpoint(firstDay))
                        .with(authenticatedAs(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validActivityRequest(2)
                        )))
                .andExpect(status().isForbidden());
    }

    // move activity

    @Test
    void moveActivityShouldReturnOkWhenAdminMovesActivity()
            throws Exception {

        Map<String, Object> request = Map.of(
                "targetDayId", secondDay.getId(),
                "visitOrder", 1
        );

        mockMvc.perform(patch(
                        activitiesEndpoint(firstDay)
                                + "/" + activityId + "/move")
                        .with(authenticatedAs(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.visitOrder").value(1));
    }

    // delete activity

    @Test
    void deleteActivityShouldReturnNoContentWhenAdminDeletesActivity()
            throws Exception {

        mockMvc.perform(delete(
                        activitiesEndpoint(firstDay) + "/" + activityId)
                        .with(authenticatedAs(admin)))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                activityRepository.existsById(activityId)
        );
    }

    // security

    @Test
    void getActivitiesShouldReturnUnauthorizedWhenNotAuthenticated()
            throws Exception {

        mockMvc.perform(get(activitiesEndpoint(firstDay)))
                .andExpect(status().isUnauthorized());
    }

}
