package com.henritrip.mapper;

import com.henritrip.configuration.ModelMapperConfig;
import com.henritrip.dto.activity.ActivityRequestDTO;
import com.henritrip.dto.guideday.GuideDayRequestDTO;
import com.henritrip.dto.guideday.GuideDayResponseDTO;
import com.henritrip.model.entity.Activity;
import com.henritrip.model.entity.GuideDay;
import com.henritrip.model.enums.ActivityCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GuideDayMapper.
 * Verifies conversion between GuideDay entities and their request and response DTOs,
 * including the mapping of associated activities.
 */
class GuideDayMapperTest {

    private GuideDayMapper guideDayMapper;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapperConfig().modelMapper();
        ActivityMapper activityMapper = new ActivityMapper(modelMapper);
        guideDayMapper = new GuideDayMapper(modelMapper, activityMapper);
    }

    @Test
    void toGuideDayEntityShouldMapRequestDTOWithActivities() {
        // arrange
        GuideDayRequestDTO requestDTO = createGuideDayRequestDTO();

        // act
        GuideDay guideDay = guideDayMapper.toGuideDayEntity(requestDTO);

        // assert
        assertThat(guideDay.getDayNumber()).isEqualTo(requestDTO.getDayNumber());
        assertThat(guideDay.getActivities())
                .hasSize(1);

        Activity activity = guideDay.getActivities().get(0);
        ActivityRequestDTO activityRequest = requestDTO.getActivities().get(0);

        assertThat(activity.getTitle()).isEqualTo(activityRequest.getTitle());
        assertThat(activity.getDescription()).isEqualTo(activityRequest.getDescription());
        assertThat(activity.getCategory()).isEqualTo(activityRequest.getCategory());
        assertThat(activity.getAddress()).isEqualTo(activityRequest.getAddress());
        assertThat(activity.getVisitOrder()).isEqualTo(activityRequest.getVisitOrder());

        // relationships and generated fields must be assigned separately
        assertThat(guideDay.getId()).isNull();
        assertThat(guideDay.getGuide()).isNull();
        assertThat(activity.getGuideDay()).isNull();
        assertThat(guideDay.getCreatedAt()).isNull();
        assertThat(guideDay.getUpdatedAt()).isNull();
    }

    @Test
    void toGuideDayEntityShouldMapRequestDTOWithoutActivities() {
        // arrange
        GuideDayRequestDTO requestDTO = new GuideDayRequestDTO();
        requestDTO.setDayNumber(2);

        // act
        GuideDay guideDay = guideDayMapper.toGuideDayEntity(requestDTO);

        // assert
        assertThat(guideDay.getDayNumber()).isEqualTo(2);
        assertThat(guideDay.getActivities()).isEmpty();
    }

    @Test
    void toGuideDayResponseDTOShouldMapEntityWithActivities() {
        // arrange
        GuideDay guideDay = createGuideDay();

        // act
        GuideDayResponseDTO responseDTO =
                guideDayMapper.toGuideDayResponseDTO(guideDay);

        // assert
        assertThat(responseDTO.getId()).isEqualTo(guideDay.getId());
        assertThat(responseDTO.getDayNumber()).isEqualTo(guideDay.getDayNumber());
        assertThat(responseDTO.getCreatedAt()).isEqualTo(guideDay.getCreatedAt());
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(guideDay.getUpdatedAt());

        assertThat(responseDTO.getActivities())
                .hasSize(1);

        assertThat(responseDTO.getActivities().get(0).getId()).isEqualTo(1L);
        assertThat(responseDTO.getActivities().get(0).getTitle())
                .isEqualTo("Visit the Louvre Museum");
        assertThat(responseDTO.getActivities().get(0).getCategory())
                .isEqualTo(ActivityCategory.MUSEUM);
    }

    @Test
    void toGuideDayResponseDTOListShouldMapAllEntities() {
        // arrange
        GuideDay firstDay = createGuideDay();
        firstDay.setId(1L);
        firstDay.setDayNumber(1);

        GuideDay secondDay = createGuideDay();
        secondDay.setId(2L);
        secondDay.setDayNumber(2);

        List<GuideDay> guideDays = List.of(firstDay, secondDay);

        // act
        List<GuideDayResponseDTO> result =
                guideDayMapper.toGuideDayResponseDTOList(guideDays);

        // assert
        assertThat(result)
                .hasSize(2)
                .extracting(GuideDayResponseDTO::getId)
                .containsExactly(1L, 2L);

        assertThat(result)
                .extracting(GuideDayResponseDTO::getDayNumber)
                .containsExactly(1, 2);

        assertThat(result.get(0).getActivities()).hasSize(1);
        assertThat(result.get(1).getActivities()).hasSize(1);
    }

    private GuideDayRequestDTO createGuideDayRequestDTO() {
        ActivityRequestDTO activity = new ActivityRequestDTO();
        activity.setTitle("Visit the Louvre Museum");
        activity.setDescription("Discover the world's largest art museums");
        activity.setCategory(ActivityCategory.MUSEUM);
        activity.setAddress("Rue de Rivoli, 75001 Paris, France");
        activity.setPhoneNumber("+33 1 40 20 50 50");
        activity.setOpeningHours("Monday to Sunday, 9:00 AM - 6:00 PM");
        activity.setWebsite("https://www.louvre.fr");
        activity.setVisitOrder(1);

        GuideDayRequestDTO requestDTO = new GuideDayRequestDTO();
        requestDTO.setDayNumber(1);
        requestDTO.setActivities(List.of(activity));

        return requestDTO;
    }

    private GuideDay createGuideDay() {
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

        GuideDay guideDay = new GuideDay();
        guideDay.setId(1L);
        guideDay.setDayNumber(1);
        guideDay.setActivities(List.of(activity));

        return guideDay;
    }
}