package com.henritrip.mapper;

import com.henritrip.dto.activity.ActivityRequestDTO;
import com.henritrip.dto.activity.ActivityResponseDTO;
import com.henritrip.model.entity.Activity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Activity entity and its DTOs.
 */
@Component
public class ActivityMapper {

    private final ModelMapper modelMapper;

    public ActivityMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

        configureMappings();
    }

    // configures mappings to prevent overwriting managed relationships
    private void configureMappings() {
        modelMapper.typeMap(ActivityRequestDTO.class, Activity.class
        ).addMappings(mapper -> {
            mapper.skip(Activity::setId);
            mapper.skip(Activity::setGuideDay);
        });
    }

    // converts an Activity entity to its response DTO
    public ActivityResponseDTO toActivityResponseDTO(Activity activity) {
        return modelMapper.map(activity, ActivityResponseDTO.class);
    }

    // converts an activity creation DTO to an Activity entity
    public Activity toActivityEntity(ActivityRequestDTO requestDTO) {
        return modelMapper.map(requestDTO, Activity.class);
    }

    // converts a list of Activity entities to response DTOs
    public List<ActivityResponseDTO> toActivityResponseDTOList(List<Activity> activities) {
        return activities.stream()
                .map(this::toActivityResponseDTO)
                .collect(Collectors.toList());
    }
}
