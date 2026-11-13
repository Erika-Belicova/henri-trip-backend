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
import com.henritrip.model.entity.GuideDay;
import com.henritrip.repository.ActivityRepository;
import com.henritrip.repository.GuideDayRepository;
import com.henritrip.repository.GuideRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for creating, retrieving, moving and deleting activities.
 */
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final GuideDayRepository guideDayRepository;
    private final GuideRepository guideRepository;
    private final ActivityMapper activityMapper;

    public ActivityService(
            ActivityRepository activityRepository, GuideDayRepository guideDayRepository,
            GuideRepository guideRepository, ActivityMapper activityMapper
    ) {
        this.activityRepository = activityRepository;
        this.guideDayRepository = guideDayRepository;
        this.guideRepository = guideRepository;
        this.activityMapper = activityMapper;
    }

    // creates an activity at the requested position within a guide day
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ActivityResponseDTO createActivity(
            Long guideId, Long dayId, ActivityRequestDTO requestDTO
    ) {
        GuideDay day = findDay(guideId, dayId);

        List<Activity> activities =
                activityRepository.findByGuideDayIdOrderByVisitOrderAsc(dayId);

        validateVisitOrder(requestDTO.getVisitOrder(), activities.size() + 1);

        Activity activity = activityMapper.toActivityEntity(requestDTO);
        activity.setGuideDay(day);

        activities.add(requestDTO.getVisitOrder() - 1, activity);
        updateVisitOrders(activities);

        return activityMapper.toActivityResponseDTO(activityRepository.save(activity));
    }

    // retrieves activities belonging to a guide day in visit order
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getActivities(Long guideId, Long dayId) {
        findDay(guideId, dayId);

        return activityMapper.toActivityResponseDTOList(
                activityRepository.findByGuideDayIdOrderByVisitOrderAsc(dayId)
        );
    }

    // retrieves a specific activity belonging to a guide day
    @Transactional(readOnly = true)
    public ActivityResponseDTO getActivity(Long guideId, Long dayId, Long activityId) {
        return activityMapper.toActivityResponseDTO(
                findActivity(guideId, dayId, activityId)
        );
    }

    // deletes an activity and adjusts the remaining visit orders
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteActivity(Long guideId, Long dayId, Long activityId) {
        Activity activity = findActivity(guideId, dayId, activityId);

        List<Activity> activities =
                activityRepository.findByGuideDayIdOrderByVisitOrderAsc(dayId);

        activities.remove(activity);
        activityRepository.delete(activity);

        updateVisitOrders(activities);
    }

    // moves an activity between days or changes its position within a day
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ActivityResponseDTO moveActivity(
            Long guideId, Long sourceDayId,
            Long activityId, MoveActivityRequestDTO requestDTO
    ) {
        Activity activity = findActivity(guideId, sourceDayId, activityId);
        GuideDay targetDay = findDay(guideId, requestDTO.getTargetDayId());

        List<Activity> sourceActivities =
                activityRepository.findByGuideDayIdOrderByVisitOrderAsc(sourceDayId);

        boolean sameDay = sourceDayId.equals(targetDay.getId());

        List<Activity> targetActivities = sameDay
                ? sourceActivities
                : activityRepository.findByGuideDayIdOrderByVisitOrderAsc(targetDay.getId());

        validateVisitOrder(
                requestDTO.getVisitOrder(), sameDay ? targetActivities.size() : targetActivities.size() + 1
        );

        sourceActivities.remove(activity);

        if (!sameDay) {
            updateVisitOrders(sourceActivities);
            activity.setGuideDay(targetDay);
        }

        targetActivities.add(requestDTO.getVisitOrder() - 1, activity);
        updateVisitOrders(targetActivities);

        return activityMapper.toActivityResponseDTO(
                activityRepository.save(activity)
        );
    }

    // validates that a visit order falls within the permitted range
    private void validateVisitOrder(Integer visitOrder, int maximumPosition) {
        if (visitOrder < 1 || visitOrder > maximumPosition) {
            throw new ResourceConflictException(
                    "Visit order must be between 1 and " + maximumPosition
            );
        }
    }

    // assigns consecutive visit orders to activities
    private void updateVisitOrders(List<Activity> activities) {
        for (int i = 0; i < activities.size(); i++) {
            activities.get(i).setVisitOrder(i + 1);
        }
    }

    // retrieves a guide day belonging to the specified guide
    private GuideDay findDay(Long guideId, Long dayId) {
        if (!guideRepository.existsById(guideId)) {
            throw new GuideNotFoundException("Guide not found");
        }

        return guideDayRepository.findByIdAndGuideId(dayId, guideId)
                .orElseThrow(() -> new GuideDayNotFoundException("Guide day not found")
        );
    }

    // retrieves an activity belonging to the specified guide day
    private Activity findActivity(Long guideId, Long dayId, Long activityId) {
        findDay(guideId, dayId);

        return activityRepository.findByIdAndGuideDayId(activityId, dayId)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found")
        );
    }

}
