package com.henritrip.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * DTO representing a standardized API error response.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponseDTO {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error description", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message",
            example = "One or more validation errors occurred")
    private String message;

    @Schema(description = "Request path", example = "/api/guides")
    private String path;

    @Schema(description = "Validation errors indexed by field")
    private Map<String, String> validationErrors;

    @Schema(description = "Date and time the error occurred")
    private OffsetDateTime timestamp;
}