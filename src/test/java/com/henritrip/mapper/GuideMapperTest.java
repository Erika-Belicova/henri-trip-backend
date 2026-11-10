package com.henritrip.mapper;

import com.henritrip.configuration.ModelMapperConfig;
import com.henritrip.dto.guide.GuideRequestDTO;
import com.henritrip.dto.guide.GuideResponseDTO;
import com.henritrip.dto.guide.GuideUpdateRequestDTO;
import com.henritrip.dto.guideday.GuideDayResponseDTO;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
    void toGuideEntityShouldMapRequestDTO() {
        // arrange
        GuideRequestDTO requestDTO = createGuideRequestDTO();

        // act
        Guide guide = guideMapper.toGuideEntity(requestDTO);

        // assert
        assertThat(guide.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(guide.getDescription()).isEqualTo(requestDTO.getDescription());

        assertThat(guide.getTransports())
                .containsExactlyInAnyOrderElementsOf(requestDTO.getTransports());

        assertThat(guide.getSeasons())
                .containsExactlyInAnyOrderElementsOf(requestDTO.getSeasons());

        assertThat(guide.getAudiences())
                .containsExactlyInAnyOrderElementsOf(requestDTO.getAudiences());

        assertThat(guide.getId()).isNull();
        assertThat(guide.getCreatedBy()).isNull();

        // days are created separately
        assertThat(guide.getDays()).isEmpty();
    }

    @Test
    void toGuideResponseDTOShouldMapEntityWithDaysAndActivities() {
        // arrange
        Guide guide = createGuide();

        // act
        GuideResponseDTO responseDTO =
                guideMapper.toGuideResponseDTO(guide);

        // assert
        assertThat(responseDTO.getId()).isEqualTo(guide.getId());
        assertThat(responseDTO.getTitle()).isEqualTo(guide.getTitle());
        assertThat(responseDTO.getDescription()).isEqualTo(guide.getDescription());

        assertThat(responseDTO.getTransports())
                .containsExactlyInAnyOrderElementsOf(guide.getTransports());

        assertThat(responseDTO.getSeasons())
                .containsExactlyInAnyOrderElementsOf(guide.getSeasons());

        assertThat(responseDTO.getAudiences())
                .containsExactlyInAnyOrderElementsOf(guide.getAudiences());

        assertThat(responseDTO.getNumberOfDays()).isEqualTo(1);

        assertThat(responseDTO.getDays()).hasSize(1);

        GuideDayResponseDTO dayResponse = responseDTO.getDays().get(0);

        assertThat(dayResponse.getDayNumber()).isEqualTo(1);
        assertThat(dayResponse.getActivities()).hasSize(1);

        assertThat(dayResponse.getActivities().get(0).getTitle())
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
        requestDTO.setNumberOfDays(3);
        requestDTO.setTransports(Set.of(TransportOption.WALKING));
        requestDTO.setSeasons(Set.of(SeasonOption.SPRING));
        requestDTO.setAudiences(Set.of(AudienceOption.FRIENDS));

        // act
        guideMapper.updateGuideEntityFromDTO(requestDTO, guide);

        // assert
        assertThat(guide.getTitle()).isEqualTo(requestDTO.getTitle());
        assertThat(guide.getDescription()).isEqualTo(requestDTO.getDescription());

        // number of days is managed by GuideDayService, not GuideMapper
        assertThat(guide.getNumberOfDays()).isEqualTo(1);
        assertThat(guide.getTransports()).containsExactly(TransportOption.WALKING);
        assertThat(guide.getSeasons()).containsExactly(SeasonOption.SPRING);
        assertThat(guide.getAudiences()).containsExactly(AudienceOption.FRIENDS);

        // updating guide information must not replace its existing itinerary
        assertThat(guide.getDays()).containsExactly(existingDay);

        assertThat(guide.getDays().get(0).getActivities())
                .containsExactly(existingActivity);
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

        assertThat(result.get(0).getNumberOfDays()).isEqualTo(1);
        assertThat(result.get(1).getNumberOfDays()).isEqualTo(1);

        assertThat(result.get(0).getDays()).hasSize(1);
        assertThat(result.get(1).getDays()).hasSize(1);
    }

    private GuideRequestDTO createGuideRequestDTO() {
        GuideRequestDTO requestDTO = new GuideRequestDTO();

        requestDTO.setTitle("Paris Travel Guide");
        requestDTO.setDescription("A weekend itinerary to discover Paris");
        requestDTO.setNumberOfDays(1);

        requestDTO.setTransports(Set.of(TransportOption.WALKING, TransportOption.CAR));
        requestDTO.setSeasons(Set.of(SeasonOption.SPRING, SeasonOption.SUMMER));
        requestDTO.setAudiences(Set.of(AudienceOption.FAMILY, AudienceOption.FRIENDS));

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
        guideDay.addActivity(activity);

        Guide guide = new Guide();
        guide.setId(1L);
        guide.setTitle("Paris Travel Guide");
        guide.setDescription("A weekend itinerary to discover Paris");

        guide.setTransports(new HashSet<>(Set.of(TransportOption.WALKING, TransportOption.CAR)));
        guide.setSeasons(new HashSet<>(Set.of(SeasonOption.SPRING, SeasonOption.SUMMER)));
        guide.setAudiences(new HashSet<>(Set.of(AudienceOption.FAMILY, AudienceOption.FRIENDS)));

        guide.addDay(guideDay);

        return guide;
    }

}