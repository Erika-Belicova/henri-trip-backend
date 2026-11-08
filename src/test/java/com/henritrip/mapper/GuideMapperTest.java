package com.henritrip.mapper;

import com.henritrip.configuration.ModelMapperConfig;
import com.henritrip.dto.activity.ActivityRequestDTO;
import com.henritrip.dto.guide.GuideRequestDTO;
import com.henritrip.dto.guide.GuideResponseDTO;
import com.henritrip.dto.guide.GuideUpdateRequestDTO;
import com.henritrip.dto.guideday.GuideDayRequestDTO;
import com.henritrip.model.entity.Activity;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideDay;
import com.henritrip.model.enums.ActivityCategory;
import com.henritrip.model.enums.AudienceOption;
import com.henritrip.model.enums.SeasonOption;
import com.henritrip.model.enums.TransportOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GuideMapper.
 * Verifies conversion between Guide entities and their request and response DTOs,
 * including nested guide days and activities.
 */
class GuideMapperTest {

    private GuideMapper guideMapper;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapperConfig().modelMapper();
        ActivityMapper activityMapper = new ActivityMapper(modelMapper);
        GuideDayMapper guideDayMapper = new GuideDayMapper(modelMapper, activityMapper);
        guideMapper = new GuideMapper(modelMapper, guideDayMapper);
    }

    @Test
    void toGuideEntityShouldMapRequestDTOWithDaysAndActivities() {
        // arrange
        GuideRequestDTO requestDTO = createGuideRequestDTO();

        // act
        Guide guide = guideMapper.toGuideEntity(requestDTO);

        // assert
        assertThat(guide.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(guide.getDescription()).isEqualTo(requestDTO.getDescription());
        assertThat(guide.getTransports()).containsExactlyInAnyOrderElementsOf(requestDTO.getTransports());
        assertThat(guide.getSeasons()).containsExactlyInAnyOrderElementsOf(requestDTO.getSeasons());
        assertThat(guide.getAudiences()).containsExactlyInAnyOrderElementsOf(requestDTO.getAudiences());

        assertThat(guide.getDays()).hasSize(1);

        GuideDay guideDay = guide.getDays().get(0);
        assertThat(guideDay.getDayNumber()).isEqualTo(1);
        assertThat(guideDay.getActivities()).hasSize(1);

        Activity activity = guideDay.getActivities().get(0);
        assertThat(activity.getTitle()).isEqualTo("Visit the Louvre Museum");
        assertThat(activity.getCategory()).isEqualTo(ActivityCategory.MUSEUM);
        assertThat(activity.getVisitOrder()).isEqualTo(1);

        // relationships and generated fields must be assigned separately
        assertThat(guide.getId()).isNull();
        assertThat(guide.getCreatedBy()).isNull();
        assertThat(guideDay.getGuide()).isNull();
        assertThat(activity.getGuideDay()).isNull();
    }

    @Test
    void toGuideResponseDTOShouldMapEntityWithDaysAndActivities() {
        // arrange
        Guide guide = createGuide();

        // act
        GuideResponseDTO responseDTO = guideMapper.toGuideResponseDTO(guide);

        // assert
        assertThat(responseDTO.getId()).isEqualTo(guide.getId());
        assertThat(responseDTO.getTitle()).isEqualTo(guide.getTitle());
        assertThat(responseDTO.getDescription()).isEqualTo(guide.getDescription());
        assertThat(responseDTO.getTransports()).containsExactlyInAnyOrderElementsOf(guide.getTransports());
        assertThat(responseDTO.getSeasons()).containsExactlyInAnyOrderElementsOf(guide.getSeasons());
        assertThat(responseDTO.getAudiences()).containsExactlyInAnyOrderElementsOf(guide.getAudiences());

        assertThat(responseDTO.getDays()).hasSize(1);

        assertThat(responseDTO.getDays().get(0).getDayNumber()).isEqualTo(1);
        assertThat(responseDTO.getDays().get(0).getActivities()).hasSize(1);

        assertThat(responseDTO.getDays().get(0).getActivities().get(0).getTitle())
                .isEqualTo("Visit the Louvre Museum");
    }

    @Test
    void updateGuideEntityFromDTOShouldUpdateGuideFieldsWithoutModifyingDays() {
        // arrange
        Guide guide = createGuide();
        GuideDay existingDay = guide.getDays().get(0);
        Activity existingActivity = existingDay.getActivities().get(0);

        GuideUpdateRequestDTO requestDTO = new GuideUpdateRequestDTO();
        requestDTO.setTitle("Updated Paris Travel Guide");
        requestDTO.setDescription("Updated description");
        requestDTO.setTransports(Set.of(TransportOption.WALKING));
        requestDTO.setSeasons(Set.of(SeasonOption.SPRING));
        requestDTO.setAudiences(Set.of(AudienceOption.FRIENDS));

        // act
        guideMapper.updateGuideEntityFromDTO(requestDTO, guide);

        // assert
        assertThat(guide.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(guide.getDescription()).isEqualTo(requestDTO.getDescription());
        assertThat(guide.getTransports()).containsExactly(TransportOption.WALKING);
        assertThat(guide.getSeasons()).containsExactly(SeasonOption.SPRING);
        assertThat(guide.getAudiences()).containsExactly(AudienceOption.FRIENDS);

        // updating guide information must not replace its existing itinerary
        assertThat(guide.getDays()).containsExactly(existingDay);
        assertThat(guide.getDays().get(0).getActivities()).containsExactly(existingActivity);
    }

    @Test
    void toGuideResponseDTOListShouldMapAllEntities() {
        // arrange
        Guide firstGuide = createGuide();
        firstGuide.setId(1L);

        Guide secondGuide = createGuide();
        secondGuide.setId(2L);
        secondGuide.setTitle("London Travel Guide");

        List<Guide> guides = List.of(firstGuide, secondGuide);

        // act
        List<GuideResponseDTO> result = guideMapper.toGuideResponseDTOList(guides);

        // assert
        assertThat(result)
                .hasSize(2)
                .extracting(GuideResponseDTO::getId)
                .containsExactly(1L, 2L);

        assertThat(result)
                .extracting(GuideResponseDTO::getTitle)
                .containsExactly(firstGuide.getTitle(), secondGuide.getTitle());

        assertThat(result.get(0).getDays()).hasSize(1);
        assertThat(result.get(1).getDays()).hasSize(1);
    }

    private GuideRequestDTO createGuideRequestDTO() {
        ActivityRequestDTO activity = new ActivityRequestDTO();
        activity.setTitle("Visit the Louvre Museum");
        activity.setDescription("Discover the world's largest art museums");
        activity.setCategory(ActivityCategory.MUSEUM);
        activity.setAddress("Rue de Rivoli, 75001 Paris, France");
        activity.setVisitOrder(1);

        GuideDayRequestDTO day = new GuideDayRequestDTO();
        day.setDayNumber(1);
        day.setActivities(List.of(activity));

        GuideRequestDTO requestDTO = new GuideRequestDTO();
        requestDTO.setTitle("Paris Travel Guide");
        requestDTO.setDescription("A weekend itinerary to discover Paris");
        requestDTO.setTransports(Set.of(TransportOption.WALKING, TransportOption.CAR));
        requestDTO.setSeasons(Set.of(SeasonOption.SPRING, SeasonOption.SUMMER));
        requestDTO.setAudiences(Set.of(AudienceOption.FAMILY, AudienceOption.FRIENDS));
        requestDTO.setDays(List.of(day));

        return requestDTO;
    }

    private Guide createGuide() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setTitle("Visit the Louvre Museum");
        activity.setDescription("Discover the world's largest art museums");
        activity.setCategory(ActivityCategory.MUSEUM);
        activity.setAddress("Rue de Rivoli, 75001 Paris, France");
        activity.setVisitOrder(1);

        GuideDay guideDay = new GuideDay();
        guideDay.setId(1L);
        guideDay.setDayNumber(1);
        guideDay.setActivities(List.of(activity));

        Guide guide = new Guide();
        guide.setId(1L);
        guide.setTitle("Paris Travel Guide");
        guide.setDescription("A weekend itinerary to discover Paris");
        guide.setTransports(Set.of(TransportOption.WALKING, TransportOption.CAR));
        guide.setSeasons(Set.of(SeasonOption.SPRING, SeasonOption.SUMMER));
        guide.setAudiences(Set.of(AudienceOption.FAMILY, AudienceOption.FRIENDS));
        guide.setDays(List.of(guideDay));

        return guide;
    }
}