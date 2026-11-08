package com.henritrip.mapper;

import com.henritrip.dto.guide.GuideRequestDTO;
import com.henritrip.dto.guide.GuideResponseDTO;
import com.henritrip.dto.guide.GuideUpdateRequestDTO;
import com.henritrip.model.entity.Guide;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Guide entity and its DTOs.
 */
@Component
public class GuideMapper {

    private final ModelMapper modelMapper;
    private final GuideDayMapper guideDayMapper;

    public GuideMapper(ModelMapper modelMapper, GuideDayMapper guideDayMapper) {
        this.modelMapper = modelMapper;
        this.guideDayMapper = guideDayMapper;
    }

    // convert a Guide entity to a GuideResponseDTO with its days and activities
    public GuideResponseDTO toGuideResponseDTO(Guide guide) {
        GuideResponseDTO responseDTO = modelMapper.map(guide, GuideResponseDTO.class);

        if (guide.getDays() != null) {
            // map and set the list of guide days
            responseDTO.setDays(guideDayMapper.toGuideDayResponseDTOList(guide.getDays()));
        }

        return responseDTO;
    }

    // convert a GuideRequestDTO to a Guide entity
    public Guide toGuideEntity(GuideRequestDTO requestDTO) {
        Guide guide = modelMapper.map(requestDTO, Guide.class);

        if (requestDTO.getDays() != null) {
            // map and set the list of guide days
            guide.setDays(requestDTO.getDays().stream()
                    .map(guideDayMapper::toGuideDayEntity)
                    .collect(Collectors.toList()));
        }

        return guide;
    }

    // update an existing Guide entity from a GuideUpdateRequestDTO
    public void updateGuideEntityFromDTO(GuideUpdateRequestDTO requestDTO, Guide guide) {
        modelMapper.map(requestDTO, guide);
    }

    // convert a list of Guide entities to a list of GuideResponseDTOs
    public List<GuideResponseDTO> toGuideResponseDTOList(List<Guide> guides) {
        return guides.stream()
                .map(this::toGuideResponseDTO)
                .collect(Collectors.toList());
    }

}
