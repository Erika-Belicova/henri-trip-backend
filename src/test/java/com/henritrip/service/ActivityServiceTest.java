package com.henritrip.service;

import com.henritrip.dto.activity.ActivityRequestDTO;
import com.henritrip.dto.activity.ActivityResponseDTO;
import com.henritrip.dto.activity.MoveActivityRequestDTO;
import com.henritrip.exception.ActivityNotFoundException;
import com.henritrip.exception.GuideDayNotFoundException;
import com.henritrip.exception.GuideNotFoundException;
import com.henritrip.exception.ResourceConflictException;
import com.henritrip.mapper.ActivityMapper;
import com.henritrip.model.entity.Activity;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideDay;
import com.henritrip.repository.ActivityRepository;
import com.henritrip.repository.GuideDayRepository;
import com.henritrip.repository.GuideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private GuideDayRepository guideDayRepository;

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private ActivityService activityService;

    private Guide guide;
    private GuideDay firstDay;
    private GuideDay secondDay;

    @BeforeEach
    void setUp() {
        guide = new Guide();
        guide.setId(1L);

        firstDay = new GuideDay();
        firstDay.setId(10L);
        firstDay.setGuide(guide);
        firstDay.setDayNumber(1);

        secondDay = new GuideDay();
        secondDay.setId(20L);
        secondDay.setGuide(guide);
        secondDay.setDayNumber(2);
    }

    // create activity

    @Test
    void createActivityShouldCreateActivityAtRequestedPosition() {
        ActivityRequestDTO request = mock(ActivityRequestDTO.class);
        when(request.getVisitOrder()).thenReturn(2);

        Activity existing = createActivity(100L, 1);

        Activity newActivity = new Activity();
        ActivityResponseDTO response = new ActivityResponseDTO();

        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(activityRepository.findByGuideDayIdOrderByVisitOrderAsc(10L))
                .thenReturn(new java.util.ArrayList<>(List.of(existing)));

        when(activityMapper.toActivityEntity(request))
                .thenReturn(newActivity);

        when(activityRepository.save(newActivity))
                .thenReturn(newActivity);

        when(activityMapper.toActivityResponseDTO(newActivity))
                .thenReturn(response);

        ActivityResponseDTO result = activityService.createActivity(
                1L, 10L, request
        );

        assertSame(response, result);
        assertEquals(1, existing.getVisitOrder());
        assertEquals(2, newActivity.getVisitOrder());
        assertSame(firstDay, newActivity.getGuideDay());

        verify(activityRepository).save(newActivity);
    }

    @Test
    void createActivityShouldRejectInvalidVisitOrder() {
        ActivityRequestDTO request = mock(ActivityRequestDTO.class);
        when(request.getVisitOrder()).thenReturn(3);

        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(activityRepository.findByGuideDayIdOrderByVisitOrderAsc(10L))
                .thenReturn(List.of(createActivity(100L, 1)));

        assertThrows(
                ResourceConflictException.class,
                () -> activityService.createActivity(1L, 10L, request)
        );

        verify(activityRepository, never()).save(any());
    }

    // get activities

    @Test
    void getActivitiesShouldReturnActivities() {
        List<Activity> activities = List.of(
                createActivity(100L, 1),
                createActivity(101L, 2)
        );

        List<ActivityResponseDTO> responses = List.of(
                new ActivityResponseDTO(),
                new ActivityResponseDTO()
        );

        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(activityRepository.findByGuideDayIdOrderByVisitOrderAsc(10L))
                .thenReturn(activities);

        when(activityMapper.toActivityResponseDTOList(activities))
                .thenReturn(responses);

        List<ActivityResponseDTO> result =
                activityService.getActivities(1L, 10L);

        assertEquals(2, result.size());
        assertSame(responses, result);
    }

    @Test
    void getActivitiesShouldThrowWhenGuideDoesNotExist() {
        when(guideRepository.existsById(1L)).thenReturn(false);

        assertThrows(
                GuideNotFoundException.class,
                () -> activityService.getActivities(1L, 10L)
        );
    }

    @Test
    void getActivitiesShouldThrowWhenDayDoesNotBelongToGuide() {
        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                GuideDayNotFoundException.class,
                () -> activityService.getActivities(1L, 10L)
        );
    }

    // get activity

    @Test
    void getActivityShouldReturnActivity() {
        Activity activity = createActivity(100L, 1);
        ActivityResponseDTO response = new ActivityResponseDTO();

        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(activityRepository.findByIdAndGuideDayId(100L, 10L))
                .thenReturn(Optional.of(activity));

        when(activityMapper.toActivityResponseDTO(activity))
                .thenReturn(response);

        ActivityResponseDTO result =
                activityService.getActivity(1L, 10L, 100L);

        assertSame(response, result);
    }

    @Test
    void getActivityShouldThrowWhenActivityDoesNotExist() {
        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(activityRepository.findByIdAndGuideDayId(100L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ActivityNotFoundException.class,
                () -> activityService.getActivity(1L, 10L, 100L)
        );
    }

    // delete activity

    @Test
    void deleteActivityShouldDeleteActivityAndUpdateVisitOrders() {
        Activity first = createActivity(100L, 1);
        Activity second = createActivity(101L, 2);
        Activity third = createActivity(102L, 3);

        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(activityRepository.findByIdAndGuideDayId(101L, 10L))
                .thenReturn(Optional.of(second));

        when(activityRepository.findByGuideDayIdOrderByVisitOrderAsc(10L))
                .thenReturn(new java.util.ArrayList<>(
                        List.of(first, second, third)
                ));

        activityService.deleteActivity(1L, 10L, 101L);

        assertEquals(1, first.getVisitOrder());
        assertEquals(2, third.getVisitOrder());

        verify(activityRepository).delete(second);
    }

    // move activity

    @Test
    void moveActivityShouldMoveActivityToAnotherDay() {
        Activity activity = createActivity(100L, 1);
        activity.setGuideDay(firstDay);

        Activity targetActivity = createActivity(200L, 1);
        targetActivity.setGuideDay(secondDay);

        MoveActivityRequestDTO request =
                mock(MoveActivityRequestDTO.class);

        when(request.getTargetDayId()).thenReturn(20L);
        when(request.getVisitOrder()).thenReturn(2);

        when(guideRepository.existsById(1L)).thenReturn(true);

        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(guideDayRepository.findByIdAndGuideId(20L, 1L))
                .thenReturn(Optional.of(secondDay));

        when(activityRepository.findByIdAndGuideDayId(100L, 10L))
                .thenReturn(Optional.of(activity));

        when(activityRepository.findByGuideDayIdOrderByVisitOrderAsc(10L))
                .thenReturn(new java.util.ArrayList<>(
                        List.of(activity)
                ));

        when(activityRepository.findByGuideDayIdOrderByVisitOrderAsc(20L))
                .thenReturn(new java.util.ArrayList<>(
                        List.of(targetActivity)
                ));

        when(activityRepository.save(activity)).thenReturn(activity);

        ActivityResponseDTO response = new ActivityResponseDTO();

        when(activityMapper.toActivityResponseDTO(activity))
                .thenReturn(response);

        ActivityResponseDTO result = activityService.moveActivity(
                1L, 10L, 100L, request
        );

        assertSame(response, result);
        assertSame(secondDay, activity.getGuideDay());
        assertEquals(2, activity.getVisitOrder());

        verify(activityRepository).save(activity);
    }

    @Test
    void moveActivityShouldReorderActivityWithinSameDay() {
        Activity first = createActivity(100L, 1);
        Activity second = createActivity(101L, 2);
        Activity third = createActivity(102L, 3);

        MoveActivityRequestDTO request =
                mock(MoveActivityRequestDTO.class);

        when(request.getTargetDayId()).thenReturn(10L);
        when(request.getVisitOrder()).thenReturn(1);

        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(activityRepository.findByIdAndGuideDayId(102L, 10L))
                .thenReturn(Optional.of(third));

        when(activityRepository.findByGuideDayIdOrderByVisitOrderAsc(10L))
                .thenReturn(new java.util.ArrayList<>(
                        List.of(first, second, third)
                ));

        when(activityRepository.save(third)).thenReturn(third);

        when(activityMapper.toActivityResponseDTO(third))
                .thenReturn(new ActivityResponseDTO());

        activityService.moveActivity(1L, 10L, 102L, request);

        assertEquals(1, third.getVisitOrder());
        assertEquals(2, first.getVisitOrder());
        assertEquals(3, second.getVisitOrder());
    }

    @Test
    void moveActivityShouldRejectInvalidVisitOrder() {
        Activity activity = createActivity(100L, 1);

        MoveActivityRequestDTO request =
                mock(MoveActivityRequestDTO.class);

        when(request.getTargetDayId()).thenReturn(10L);
        when(request.getVisitOrder()).thenReturn(3);

        when(guideRepository.existsById(1L)).thenReturn(true);
        when(guideDayRepository.findByIdAndGuideId(10L, 1L))
                .thenReturn(Optional.of(firstDay));

        when(activityRepository.findByIdAndGuideDayId(100L, 10L))
                .thenReturn(Optional.of(activity));

        when(activityRepository.findByGuideDayIdOrderByVisitOrderAsc(10L))
                .thenReturn(new java.util.ArrayList<>(
                        List.of(activity)
                ));

        assertThrows(
                ResourceConflictException.class,
                () -> activityService.moveActivity(
                        1L, 10L, 100L, request
                )
        );

        verify(activityRepository, never()).save(any());
    }

    // helper

    private Activity createActivity(Long id, int visitOrder) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setVisitOrder(visitOrder);
        return activity;
    }

}
