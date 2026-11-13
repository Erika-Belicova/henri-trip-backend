package com.henritrip.dto.activity;

import com.henritrip.model.enums.ActivityCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * DTO for creating an activity within a guide day.
 * Contains activity details, location information and its visit order.
 */
@Data
public class ActivityRequestDTO {

    @NotBlank(message = "Activity title cannot be blank")
    @Size(max = 200, message = "Activity title cannot exceed 200 characters")
    @Schema(description = "Title of the activity", example = "Visit the Louvre Museum")
    private String title;

    @NotBlank(message = "Activity description cannot be blank")
    @Size(max = 5000, message = "Activity description cannot exceed 5000 characters")
    @Schema(description = "Description of the activity",
            example = "Discover one of the world's largest art museums")
    private String description;

    @NotNull(message = "Activity category is required")
    @Schema(description = "Category of the activity", example = "MUSEUM")
    private ActivityCategory category;

    @NotBlank(message = "Activity address cannot be blank")
    @Size(max = 500, message = "Activity address cannot exceed 500 characters")
    @Schema(description = "Address of the activity",
            example = "Rue de Rivoli, 75001 Paris, France")
    private String address;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    @Schema(description = "Contact phone number", example = "+33 1 40 20 50 50")
    private String phoneNumber;

    @Size(max = 500, message = "Opening hours cannot exceed 500 characters")
    @Schema(description = "Opening hours of the activity",
            example = "Monday to Sunday, 9:00 AM - 6:00 PM")
    private String openingHours;

    @Size(max = 2048, message = "Website cannot exceed 2048 characters")
    @URL(regexp = "^(http|https)://.*", message = "Website must be a valid http(s) URL")
    @Schema(description = "Official website of the activity",
            example = "https://www.louvre.fr")
    private String website;

    @NotNull(message = "Visit order is required")
    @Positive(message = "Visit order must be positive")
    @Schema(description = "Position of the activity within the day", example = "1")
    private Integer visitOrder;

}