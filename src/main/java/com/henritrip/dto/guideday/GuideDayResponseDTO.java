package com.henritrip.dto.guideday;

import com.henritrip.dto.activity.ActivityResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO representing a day within a travel guide.
 * Exposes the day's number, associated activities and audit timestamps.
 */
@Data
public class GuideDayResponseDTO {

    @Schema(description = "Unique identifier of the guide day", example = "1")
    private Long id;

    @Schema(description = "Position of the day within the guide", example = "1")
    private Integer dayNumber;

    @Schema(description = "Activities scheduled for this day")
    private List<ActivityResponseDTO> activities;

    @Schema(description = "Date and time the guide day was created")
    private OffsetDateTime createdAt;

    @Schema(description = "Date and time the guide day was last updated")
    private OffsetDateTime updatedAt;

}
