package com.henritrip.repository;

import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideDay;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class GuideDayRepositoryTest {

    @Autowired
    private GuideDayRepository guideDayRepository;

    @Autowired
    private GuideRepository guideRepository;

    @Test
    void findByGuideIdOrderByDayNumberAscShouldReturnDaysInOrder() {
        // arrange
        Guide guide = createGuide("Paris Travel Guide");

        createGuideDay(guide, 3);
        createGuideDay(guide, 1);
        createGuideDay(guide, 2);

        // act
        var result = guideDayRepository.findByGuideIdOrderByDayNumberAsc(guide.getId());

        // assert
        assertThat(result)
                .extracting(GuideDay::getDayNumber)
                .containsExactly(1, 2, 3);
    }

    @Test
    void findByIdAndGuideIdShouldReturnDayOnlyForMatchingGuide() {
        // arrange
        Guide guide = createGuide("Paris Travel Guide");
        Guide anotherGuide = createGuide("London Travel Guide");

        GuideDay guideDay = createGuideDay(guide, 1);

        // act & assert
        assertThat(guideDayRepository.findByIdAndGuideId(
                guideDay.getId(), guide.getId()
        )).isPresent();

        assertThat(guideDayRepository.findByIdAndGuideId(
                guideDay.getId(), anotherGuide.getId()
        )).isEmpty();

        assertThat(guideDayRepository.findByIdAndGuideId(
                999L, guide.getId()
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
}
