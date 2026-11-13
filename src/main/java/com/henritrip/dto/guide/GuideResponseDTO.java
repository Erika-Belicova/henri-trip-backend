package com.henritrip.dto.guide;

import com.henritrip.dto.guideday.GuideDayResponseDTO;
import com.henritrip.model.enums.AudienceOption;
import com.henritrip.model.enums.SeasonOption;
import com.henritrip.model.enums.TransportOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO representing a travel guide with its complete itinerary.
 * Includes descriptive information, available options and associated days.
 */
@Data
public class GuideResponseDTO {

    @Schema(description = "Unique identifier of the guide", example = "1")
    private Long id;

    @Schema(description = "Title of the guide", example = "Weekend in Paris")
    private String title;

    @Schema(description = "Description of the guide")
    private String description;

    @Schema(description = "Total number of days in the guide", example = "2")
    private Integer numberOfDays;

    @Schema(description = "Available transportation options")
    private Set<TransportOption> transports;

    @Schema(description = "Seasons during which the guide is suitable")
    private Set<SeasonOption> seasons;

    @Schema(description = "Intended audiences for the guide")
    private Set<AudienceOption> audiences;

    @Schema(description = "Days included in the guide")
    private List<GuideDayResponseDTO> days;

    @Schema(description = "Date and time the guide was created")
    private OffsetDateTime createdAt;

    @Schema(description = "Date and time the guide was last updated")
    private OffsetDateTime updatedAt;

}