package com.henritrip.mapper;

import com.henritrip.dto.guide.GuideRequestDTO;
import com.henritrip.dto.guide.GuideResponseDTO;
import com.henritrip.dto.guide.GuideUpdateRequestDTO;
import com.henritrip.model.entity.Guide;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Guide entities and their request and response DTOs.
 * Handles guide information, day counts and the mapping of associated guide days.
 */
@Component
public class GuideMapper {

    private final ModelMapper modelMapper;
    private final GuideDayMapper guideDayMapper;

    public GuideMapper(ModelMapper modelMapper, GuideDayMapper guideDayMapper) {
        this.modelMapper = modelMapper;
        this.guideDayMapper = guideDayMapper;

        configureMappings();
    }

    // prevents ModelMapper from handling days automatically
    private void configureMappings() {
        modelMapper.typeMap(GuideRequestDTO.class, Guide.class).addMappings(
                mapper ->
                mapper.skip(Guide::setDays)
        );
    }

    // converts a Guide entity to a response DTO with its itinerary
    public GuideResponseDTO toGuideResponseDTO(Guide guide) {
        GuideResponseDTO responseDTO = modelMapper.map(guide, GuideResponseDTO.class);

        responseDTO.setNumberOfDays(guide.getDays().size());

        responseDTO.setDays(guideDayMapper.toGuideDayResponseDTOList(guide.getDays())
        );

        return responseDTO;
    }

    // converts a guide creation DTO to a Guide entity
    public Guide toGuideEntity(GuideRequestDTO requestDTO) {
        return modelMapper.map(requestDTO, Guide.class);
    }

    // updates guide information without modifying its itinerary
    public void updateGuideEntityFromDTO(GuideUpdateRequestDTO requestDTO, Guide guide) {

        guide.setTitle(requestDTO.getTitle());
        guide.setDescription(requestDTO.getDescription());

        replaceCollection(guide.getTransports(), requestDTO.getTransports());
        replaceCollection(guide.getSeasons(), requestDTO.getSeasons());
        replaceCollection(guide.getAudiences(), requestDTO.getAudiences());
    }

    // replaces collection contents without replacing the collection instance
    private <T> void replaceCollection(Set<T> target, Set<T> source) {
        target.clear();
        target.addAll(source);
    }

    // converts a list of Guide entities to response DTOs
    public List<GuideResponseDTO> toGuideResponseDTOList(List<Guide> guides) {
        return guides.stream()
                .map(this::toGuideResponseDTO)
                .collect(Collectors.toList());
    }

}
