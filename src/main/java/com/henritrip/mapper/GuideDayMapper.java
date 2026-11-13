package com.henritrip.mapper;

import com.henritrip.dto.guideday.GuideDayResponseDTO;
import com.henritrip.model.entity.GuideDay;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between GuideDay entities and their response DTOs,
 * including the mapping of associated activities.
 */
@Component
public class GuideDayMapper {

    private final ModelMapper modelMapper;
    private final ActivityMapper activityMapper;

    public GuideDayMapper(ModelMapper modelMapper, ActivityMapper activityMapper) {
        this.modelMapper = modelMapper;
        this.activityMapper = activityMapper;
    }

    public GuideDayResponseDTO toGuideDayResponseDTO(GuideDay guideDay) {
        GuideDayResponseDTO responseDTO = modelMapper.map(
                guideDay, GuideDayResponseDTO.class
        );

        if (guideDay.getActivities() != null) {
            responseDTO.setActivities(activityMapper.toActivityResponseDTOList(
                    guideDay.getActivities())
            );
        }
        return responseDTO;
    }

    public List<GuideDayResponseDTO> toGuideDayResponseDTOList(List<GuideDay> guideDays) {
        return guideDays.stream()
                .map(this::toGuideDayResponseDTO)
                .collect(Collectors.toList());
    }

}
