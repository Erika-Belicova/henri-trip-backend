package com.henritrip.dto.activity;

import com.henritrip.model.enums.ActivityCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * DTO representing an activity within a guide day.
 * Exposes activity details, location information, visit order and audit timestamps.
 */
@Data
public class ActivityResponseDTO {

    @Schema(description = "Unique identifier of the activity", example = "1")
    private Long id;

    @Schema(description = "Title of the activity", example = "Visit the Louvre Museum")
    private String title;

    @Schema(description = "Description of the activity")
    private String description;

    @Schema(description = "Category of the activity")
    private ActivityCategory category;

    @Schema(description = "Address of the activity")
    private String address;

    @Schema(description = "Contact phone number", example = "+33 6 40 20 50 50")
    private String phoneNumber;

    @Schema(description = "Opening hours of the activity")
    private String openingHours;

    @Schema(description = "Official website of the activity")
    private String website;

    @Schema(description = "Position of the activity within the day", example = "1")
    private Integer visitOrder;

    @Schema(description = "Date and time the activity was created")
    private OffsetDateTime createdAt;

    @Schema(description = "Date and time the activity was last updated")
    private OffsetDateTime updatedAt;

}
