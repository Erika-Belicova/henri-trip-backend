package com.henritrip.dto.guide;

import com.henritrip.model.enums.AudienceOption;
import com.henritrip.model.enums.SeasonOption;
import com.henritrip.model.enums.TransportOption;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * DTO for updating a guide's descriptive information and options.
 * Days and activities are managed through their own endpoints.
 */
@Data
public class GuideUpdateRequestDTO {

    @NotBlank(message = "Guide title cannot be blank")
    @Size(max = 200, message = "Guide title cannot exceed 200 characters")
    @Schema(description = "Title of the guide", example = "Weekend in Paris")
    private String title;

    @NotBlank(message = "Guide description cannot be blank")
    @Size(max = 5000, message = "Guide description cannot exceed 5000 characters")
    @Schema(description = "Description of the guide",
            example = "A weekend itinerary to discover Paris")
    private String description;

    @NotEmpty(message = "At least one transport option is required")
    @Schema(description = "Available transportation options")
    private Set<@NotNull TransportOption> transports;

    @NotEmpty(message = "At least one season is required")
    @Schema(description = "Seasons during which the guide is suitable")
    private Set<@NotNull SeasonOption> seasons;

    @NotEmpty(message = "At least one audience option is required")
    @Schema(description = "Intended audiences for the guide")
    private Set<@NotNull AudienceOption> audiences;

}