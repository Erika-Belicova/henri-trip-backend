package com.henritrip.repository;

import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.Activity;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideDay;
import com.henritrip.model.enums.ActivityCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private GuideDayRepository guideDayRepository;

    @Test
    void findByGuideDayIdOrderByVisitOrderAscShouldReturnActivitiesInOrder() {
        // arrange
        Guide guide = createGuide("Paris Travel Guide");
        GuideDay guideDay = createGuideDay(guide, 1);

        createActivity(guideDay, "Eiffel Tower", 3);
        createActivity(guideDay, "Louvre Museum", 1);
        createActivity(guideDay, "Notre-Dame Cathedral", 2);

        // act
        var result = activityRepository.findByGuideDayIdOrderByVisitOrderAsc(
                guideDay.getId()
        );

        // assert
        assertThat(result)
                .extracting(Activity::getVisitOrder)
                .containsExactly(1, 2, 3);
    }

    @Test
    void findByIdAndGuideDayIdShouldReturnActivityOnlyForMatchingDay() {
        // arrange
        Guide guide = createGuide("Paris Travel Guide");

        GuideDay guideDay = createGuideDay(guide, 1);
        GuideDay anotherGuideDay = createGuideDay(guide, 2);

        Activity activity = createActivity(guideDay, "Louvre Museum", 1);

        // act & assert
        assertThat(activityRepository.findByIdAndGuideDayId(
                activity.getId(), guideDay.getId()
        )).isPresent();

        assertThat(activityRepository.findByIdAndGuideDayId(
                activity.getId(), anotherGuideDay.getId()
        )).isEmpty();

        assertThat(activityRepository.findByIdAndGuideDayId(
                999L, guideDay.getId()
        )).isEmpty();
    }

    private Guide createGuide(String title) {
        Guide guide = new Guide();
        guide.setTitle(title);
        guide.setDescription("Test guide description");

        return guideRepository.save(guide);
    }

    private GuideDay createGuideDay(Guide guide, int dayNumber) {
        GuideDay guideDay = new GuideDay();
        guideDay.setGuide(guide);
        guideDay.setDayNumber(dayNumber);

        return guideDayRepository.save(guideDay);
    }

    private Activity createActivity(
            GuideDay guideDay,
            String title,
            int visitOrder
    ) {
        Activity activity = new Activity();
        activity.setGuideDay(guideDay);
        activity.setTitle(title);
        activity.setDescription("Test activity description");
        activity.setCategory(ActivityCategory.ACTIVITY);
        activity.setAddress("Paris, France");
        activity.setVisitOrder(visitOrder);

        return activityRepository.save(activity);
    }
}
