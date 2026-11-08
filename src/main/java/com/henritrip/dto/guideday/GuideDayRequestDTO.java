package com.henritrip.dto.guideday;

import com.henritrip.dto.activity.ActivityRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * DTO for creating or updating a day within a travel guide.
 * Contains the day's number and its associated activities.
 */
@Data
public class GuideDayRequestDTO {

    @NotNull(message = "Day number is required")
    @Positive(message = "Day number must be positive")
    @Schema(description = "Position of the day within the guide", example = "1")
    private Integer dayNumber;

    @Valid
    @Schema(description = "Activities scheduled for this day")
    private List<@NotNull ActivityRequestDTO> activities;

}