package com.henritrip.controller;

import com.henritrip.dto.activity.ActivityRequestDTO;
import com.henritrip.dto.activity.ActivityResponseDTO;
import com.henritrip.dto.activity.MoveActivityRequestDTO;
import com.henritrip.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing activities within guide days.
 * Provides endpoints for creating, retrieving, moving and deleting activities.
 */
@Tag(name = "Activities", description = "Activity management endpoints")
@RestController
@RequestMapping("/api/guides/{guideId}/days/{dayId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Operation(summary = "Retrieve activities of a guide day")
    @GetMapping
    public ResponseEntity<@NonNull List<ActivityResponseDTO>> getActivities(
            @PathVariable Long guideId,
            @PathVariable Long dayId
    ) {
        return ResponseEntity.ok(
                activityService.getActivities(guideId, dayId)
        );
    }

    @Operation(summary = "Retrieve an activity")
    @GetMapping("/{activityId}")
    public ResponseEntity<@NonNull ActivityResponseDTO> getActivity(
            @PathVariable Long guideId,
            @PathVariable Long dayId,
            @PathVariable Long activityId
    ) {
        return ResponseEntity.ok(
                activityService.getActivity(guideId, dayId, activityId)
        );
    }

    @Operation(summary = "Create an activity")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<@NonNull ActivityResponseDTO> createActivity(
            @PathVariable Long guideId,
            @PathVariable Long dayId,
            @RequestBody @Valid ActivityRequestDTO requestDTO
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                activityService.createActivity(guideId, dayId, requestDTO)
        );
    }

    @Operation(summary = "Move an activity to another guide day")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{activityId}/move")
    public ResponseEntity<@NonNull ActivityResponseDTO> moveActivity(
            @PathVariable Long guideId,
            @PathVariable Long dayId,
            @PathVariable Long activityId,
            @RequestBody @Valid MoveActivityRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(
                activityService.moveActivity(guideId, dayId, activityId, requestDTO)
        );
    }

    @Operation(summary = "Delete an activity")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{activityId}")
    public ResponseEntity<@NonNull Void> deleteActivity(
            @PathVariable Long guideId,
            @PathVariable Long dayId,
            @PathVariable Long activityId
    ) {
        activityService.deleteActivity(guideId, dayId, activityId);

        return ResponseEntity.noContent().build();
    }

}
