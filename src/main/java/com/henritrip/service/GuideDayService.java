package com.henritrip.service;

import com.henritrip.dto.guideday.GuideDayResponseDTO;
import com.henritrip.exception.GuideDayNotFoundException;
import com.henritrip.exception.GuideNotFoundException;
import com.henritrip.exception.ResourceConflictException;
import com.henritrip.mapper.GuideDayMapper;
import com.henritrip.model.entity.Guide;
import com.henritrip.model.entity.GuideDay;
import com.henritrip.repository.GuideDayRepository;
import com.henritrip.repository.GuideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for managing guide days and their associated activities.
 */
@Service
public class GuideDayService {

    private final GuideDayRepository guideDayRepository;
    private final GuideRepository guideRepository;
    private final GuideDayMapper guideDayMapper;

    public GuideDayService(
            GuideDayRepository guideDayRepository, GuideRepository guideRepository,
            GuideDayMapper guideDayMapper
    ) {
        this.guideDayRepository = guideDayRepository;
        this.guideRepository = guideRepository;
        this.guideDayMapper = guideDayMapper;
    }

    // retrieves all days belonging to a guide in itinerary order
    @Transactional(readOnly = true)
    public List<GuideDayResponseDTO> getDays(Long guideId) {
        if (!guideRepository.existsById(guideId)) {
            throw new GuideNotFoundException("Guide not found");
        }

        return guideDayMapper.toGuideDayResponseDTOList(
                guideDayRepository.findByGuideIdOrderByDayNumberAsc(guideId)
        );
    }

    // retrieves a specific day belonging to a guide
    @Transactional(readOnly = true)
    public GuideDayResponseDTO getDay(Long guideId, Long dayId) {
        if (!guideRepository.existsById(guideId)) {
            throw new GuideNotFoundException("Guide not found");
        }

        GuideDay day = guideDayRepository.findByIdAndGuideId(dayId, guideId
        ).orElseThrow(() -> new GuideDayNotFoundException("Guide day not found"));

        return guideDayMapper.toGuideDayResponseDTO(day);
    }

    // synchronises the number of days with the requested guide duration
    @Transactional
    public void synchronizeDays(Guide guide, Integer requestedDays) {
        int currentDays = guide.getDays().size();

        if (requestedDays < currentDays) {
            removeExcessDays(guide, requestedDays);
        } else if (requestedDays > currentDays) {
            addMissingDays(guide, requestedDays);
        }
    }

    // removes excess days while preserving existing activities
    private void removeExcessDays(Guide guide, Integer requestedDays) {
        List<GuideDay> days = guide.getDays();
        validateDaysCanBeRemoved(days, requestedDays);

        while (days.size() > requestedDays) {
            guide.removeDay(days.get(days.size() - 1));
        }
    }

    // prevents accidental deletion of activities
    private void validateDaysCanBeRemoved(List<GuideDay> days, Integer requestedDays) {
        for (int i = requestedDays; i < days.size(); i++) {
            GuideDay day = days.get(i);

            if (!day.getActivities().isEmpty()) {
                throw new ResourceConflictException(
                        "Cannot remove day " + day.getDayNumber()
                                + " because it contains activities. "
                                + "Move or delete those activities first."
                );
            }
        }
    }

    // adds missing days at the end of the itinerary
    private void addMissingDays(Guide guide, Integer requestedDays) {
        while (guide.getDays().size() < requestedDays) {

            GuideDay day = new GuideDay();
            day.setDayNumber(guide.getDays().size() + 1);

            guide.addDay(day);
        }
    }

}
