package com.henritrip.service;

import com.henritrip.exception.ResourceConflictException;
import com.henritrip.mapper.GuideDayMapper;
import com.henritrip.model.entity.Activity;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideDay;
import com.henritrip.repository.GuideDayRepository;
import com.henritrip.repository.GuideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GuideDayServiceTest {

    private GuideDayService guideDayService;

    private Guide createGuideWithDays(int numberOfDays) {
        Guide guide = new Guide();

        for (int i = 1; i <= numberOfDays; i++) {
            GuideDay day = new GuideDay();
            day.setDayNumber(i);
            guide.addDay(day);
        }

        return guide;
    }

    @BeforeEach
    void setUp() {

        guideDayService = new GuideDayService(
                mock(GuideDayRepository.class),
                mock(GuideRepository.class),
                mock(GuideDayMapper.class)
        );
    }

    @Test
    void synchronizeDaysShouldAddMissingDays() {
        Guide guide = createGuideWithDays(3);

        guideDayService.synchronizeDays(guide, 5);

        assertEquals(5, guide.getDays().size());

        assertEquals(
                List.of(1, 2, 3, 4, 5),
                guide.getDays().stream()
                        .map(GuideDay::getDayNumber)
                        .toList()
        );

        assertTrue(
                guide.getDays().stream()
                        .allMatch(day -> day.getGuide() == guide)
        );
    }

    @Test
    void synchronizeDaysShouldRemoveExcessDays() {
        Guide guide = createGuideWithDays(5);

        GuideDay fourthDay = guide.getDays().get(3);
        GuideDay fifthDay = guide.getDays().get(4);

        guideDayService.synchronizeDays(guide, 2);

        assertEquals(2, guide.getDays().size());

        assertEquals(
                List.of(1, 2),
                guide.getDays().stream()
                        .map(GuideDay::getDayNumber)
                        .toList()
        );

        assertNull(fourthDay.getGuide());
        assertNull(fifthDay.getGuide());
    }

    @Test
    void synchronizeDaysShouldPreserveExistingDays() {
        Guide guide = createGuideWithDays(3);

        GuideDay firstDay = guide.getDays().get(0);
        GuideDay secondDay = guide.getDays().get(1);
        GuideDay thirdDay = guide.getDays().get(2);

        guideDayService.synchronizeDays(guide, 5);

        assertSame(firstDay, guide.getDays().get(0));
        assertSame(secondDay, guide.getDays().get(1));
        assertSame(thirdDay, guide.getDays().get(2));
    }

    @Test
    void synchronizeDaysShouldNotChangeDaysWhenDurationIsUnchanged() {
        Guide guide = createGuideWithDays(3);

        GuideDay firstDay = guide.getDays().get(0);
        GuideDay secondDay = guide.getDays().get(1);
        GuideDay thirdDay = guide.getDays().get(2);

        guideDayService.synchronizeDays(guide, 3);

        assertEquals(3, guide.getDays().size());

        assertSame(firstDay, guide.getDays().get(0));
        assertSame(secondDay, guide.getDays().get(1));
        assertSame(thirdDay, guide.getDays().get(2));
    }

    @Test
    void synchronizeDaysShouldRejectRemovalOfDaysContainingActivities() {
        Guide guide = createGuideWithDays(3);

        GuideDay thirdDay = guide.getDays().get(2);
        thirdDay.addActivity(new Activity());

        assertThrows(
                ResourceConflictException.class,
                () -> guideDayService.synchronizeDays(guide, 2)
        );

        assertEquals(3, guide.getDays().size());

        assertSame(thirdDay, guide.getDays().get(2));
        assertSame(guide, thirdDay.getGuide());
        assertEquals(1, thirdDay.getActivities().size());
    }

    @Test
    void synchronizeDaysShouldAllowRemovalWhenActivitiesAreOnPreservedDays() {
        Guide guide = createGuideWithDays(3);

        GuideDay firstDay = guide.getDays().get(0);
        firstDay.addActivity(new Activity());

        guideDayService.synchronizeDays(guide, 2);

        assertEquals(2, guide.getDays().size());
        assertSame(firstDay, guide.getDays().get(0));
        assertEquals(1, firstDay.getActivities().size());
    }

}
