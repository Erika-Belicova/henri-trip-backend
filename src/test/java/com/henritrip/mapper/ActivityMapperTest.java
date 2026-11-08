package com.henritrip.mapper;

import com.henritrip.configuration.ModelMapperConfig;
import com.henritrip.dto.activity.ActivityRequestDTO;
import com.henritrip.dto.activity.ActivityResponseDTO;
import com.henritrip.model.entity.Activity;
import com.henritrip.model.enums.ActivityCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ActivityMapper.
 * Verifies conversion between Activity entities and their request and response DTOs.
 */
class ActivityMapperTest {

    private ActivityMapper activityMapper;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapperConfig().modelMapper();
        activityMapper = new ActivityMapper(modelMapper);
    }

    @Test
    void toActivityEntityShouldMapRequestDTO() {
        // arrange
        ActivityRequestDTO requestDTO = createActivityRequestDTO();

        // act
        Activity activity = activityMapper.toActivityEntity(requestDTO);

        // assert
        assertThat(activity.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(activity.getDescription()).isEqualTo(requestDTO.getDescription());
        assertThat(activity.getCategory()).isEqualTo(requestDTO.getCategory());
        assertThat(activity.getAddress()).isEqualTo(requestDTO.getAddress());
        assertThat(activity.getPhoneNumber()).isEqualTo(requestDTO.getPhoneNumber());
        assertThat(activity.getOpeningHours()).isEqualTo(requestDTO.getOpeningHours());
        assertThat(activity.getWebsite()).isEqualTo(requestDTO.getWebsite());
        assertThat(activity.getVisitOrder()).isEqualTo(requestDTO.getVisitOrder());

        // relationships and generated fields must not be mapped from the request
        assertThat(activity.getId()).isNull();
        assertThat(activity.getGuideDay()).isNull();
        assertThat(activity.getCreatedAt()).isNull();
        assertThat(activity.getUpdatedAt()).isNull();
    }

    @Test
    void toActivityResponseDTOShouldMapEntity() {
        // arrange
        Activity activity = createActivity();

        // act
        ActivityResponseDTO responseDTO = activityMapper.toActivityResponseDTO(activity);

        // assert
        assertThat(responseDTO.getId()).isEqualTo(activity.getId());
        assertThat(responseDTO.getTitle()).isEqualTo(activity.getTitle());
        assertThat(responseDTO.getDescription()).isEqualTo(activity.getDescription());
        assertThat(responseDTO.getCategory()).isEqualTo(activity.getCategory());
        assertThat(responseDTO.getAddress()).isEqualTo(activity.getAddress());
        assertThat(responseDTO.getPhoneNumber()).isEqualTo(activity.getPhoneNumber());
        assertThat(responseDTO.getOpeningHours()).isEqualTo(activity.getOpeningHours());
        assertThat(responseDTO.getWebsite()).isEqualTo(activity.getWebsite());
        assertThat(responseDTO.getVisitOrder()).isEqualTo(activity.getVisitOrder());
        assertThat(responseDTO.getCreatedAt()).isEqualTo(activity.getCreatedAt());
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(activity.getUpdatedAt());
    }

    @Test
    void toActivityResponseDTOListShouldMapAllEntities() {
        // arrange
        Activity firstActivity = createActivity();
        firstActivity.setId(1L);

        Activity secondActivity = createActivity();
        secondActivity.setId(2L);
        secondActivity.setTitle("Visit the Eiffel Tower");
        secondActivity.setVisitOrder(2);

        List<Activity> activities = List.of(firstActivity, secondActivity);

        // act
        List<ActivityResponseDTO> result =
                activityMapper.toActivityResponseDTOList(activities);

        // assert
        assertThat(result)
                .hasSize(2)
                .extracting(ActivityResponseDTO::getId)
                .containsExactly(1L, 2L);

        assertThat(result)
                .extracting(ActivityResponseDTO::getTitle)
                .containsExactly(firstActivity.getTitle(), secondActivity.getTitle());
    }

    private ActivityRequestDTO createActivityRequestDTO() {
        ActivityRequestDTO requestDTO = new ActivityRequestDTO();
        requestDTO.setTitle("Visit the Louvre Museum");
        requestDTO.setDescription("Discover the world's largest art museums");
        requestDTO.setCategory(ActivityCategory.MUSEUM);
        requestDTO.setAddress("Rue de Rivoli, 75001 Paris, France");
        requestDTO.setPhoneNumber("+33 1 40 20 50 50");
        requestDTO.setOpeningHours("Monday to Sunday, 9:00 AM - 6:00 PM");
        requestDTO.setWebsite("https://www.louvre.fr");
        requestDTO.setVisitOrder(1);

        return requestDTO;
    }

    private Activity createActivity() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("Visit the Louvre Museum");
        activity.setDescription("Discover the world's largest art museums");
        activity.setCategory(ActivityCategory.MUSEUM);
        activity.setAddress("Rue de Rivoli, 75001 Paris, France");
        activity.setPhoneNumber("+33 1 40 20 50 50");
        activity.setOpeningHours("Monday to Sunday, 9:00 AM - 6:00 PM");
        activity.setWebsite("https://www.louvre.fr");
        activity.setVisitOrder(1);
        activity.setCreatedAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        activity.setUpdatedAt(OffsetDateTime.parse("2026-01-02T10:00:00Z"));

        return activity;
    }
}