package com.henritrip.mapper;

import com.henritrip.dto.guideday.GuideDayRequestDTO;
import com.henritrip.dto.guideday.GuideDayResponseDTO;
import com.henritrip.model.entity.GuideDay;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between GuideDay entity and its DTOs.
 */
@Component
public class GuideDayMapper {

    private final ModelMapper modelMapper;
    private final ActivityMapper activityMapper;

    public GuideDayMapper(ModelMapper modelMapper, ActivityMapper activityMapper) {
        this.modelMapper = modelMapper;
        this.activityMapper = activityMapper;

        configureMappings();
    }

    // configure mappings that require manual handling
    private void configureMappings() {
        modelMapper.typeMap(GuideDayRequestDTO.class, GuideDay.class)
                .addMappings(mapper ->
                        mapper.skip(GuideDay::setActivities));
    }

    // convert a GuideDay entity to a GuideDayResponseDTO with activities
    public GuideDayResponseDTO toGuideDayResponseDTO(GuideDay guideDay) {
        GuideDayResponseDTO responseDTO = modelMapper.map(guideDay, GuideDayResponseDTO.class);

        if (guideDay.getActivities() != null) {
            // map and set the list of activities
            responseDTO.setActivities(activityMapper.toActivityResponseDTOList(guideDay.getActivities()));
        }

        return responseDTO;
    }

    // convert a GuideDayRequestDTO to a GuideDay entity
    public GuideDay toGuideDayEntity(GuideDayRequestDTO requestDTO) {
        GuideDay guideDay = modelMapper.map(requestDTO, GuideDay.class);

        if (requestDTO.getActivities() != null) {
            // map and set the list of activities
            guideDay.setActivities(requestDTO.getActivities().stream()
                    .map(activityMapper::toActivityEntity)
                    .collect(Collectors.toList()));
        }

        return guideDay;
    }

    // convert a list of GuideDay entities to a list of GuideDayResponseDTOs
    public List<GuideDayResponseDTO> toGuideDayResponseDTOList(List<GuideDay> guideDays) {
        return guideDays.stream()
                .map(this::toGuideDayResponseDTO)
                .collect(Collectors.toList());
    }

}
