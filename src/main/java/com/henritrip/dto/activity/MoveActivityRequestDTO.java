package com.henritrip.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO for moving an existing activity to another day
 * or changing its position within a guide.
 */
@Data
public class MoveActivityRequestDTO {

    @NotNull(message = "Target day ID is required")
    @Positive(message = "Target day ID must be positive")
    @Schema(description = "ID of the day receiving the activity", example = "2")
    private Long targetDayId;

    @NotNull(message = "Visit order is required")
    @Positive(message = "Visit order must be positive")
    @Schema(description = "New position of the activity within the target day", example = "1")
    private Integer visitOrder;

}
