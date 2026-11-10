package com.henritrip.controller;

import com.henritrip.dto.guide.GuideRequestDTO;
import com.henritrip.dto.guide.GuideResponseDTO;
import com.henritrip.dto.guide.GuideUpdateRequestDTO;
import com.henritrip.service.GuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 REST controller responsible for guide management.
 Exposes endpoints for retrieving, creating, updating and deleting guides.
 */
@Tag(
        name = "Guides",
        description = "Guide management endpoints"
)
@SecurityRequirement(name = "Authorization")
@RestController
@RequestMapping("/api/guides")
public class GuideController {

    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    @Operation(
            summary = "Retrieve accessible guides",
            description = "Returns a paginated list of guides accessible to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guides retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<@NonNull Page<@NonNull GuideResponseDTO>> getAllGuides(
            Authentication authentication,
            Pageable pageable
    ) {
        Long userId = getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                guideService.getAllGuides(userId, pageable)
        );
    }

    @Operation(
            summary = "Retrieve a guide",
            description = "Returns a guide and its associated days and activities if the authenticated user has access."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    @GetMapping("/{guideId}")
    public ResponseEntity<@NonNull GuideResponseDTO> getGuideById(
            @PathVariable Long guideId,
            Authentication authentication
    ) {
        Long userId = getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                guideService.getGuideById(guideId, userId)
        );
    }

    @Operation(
            summary = "Create a guide",
            description = "Creates a guide with its descriptive information and options. Administrator access required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Guide created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator access required"),
            @ApiResponse(responseCode = "404", description = "Creator not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<@NonNull GuideResponseDTO> createGuide(
            @RequestBody @Valid GuideRequestDTO requestDTO,
            Authentication authentication
    ) {
        Long userId = getAuthenticatedUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guideService.createGuide(requestDTO, userId));
    }

    @Operation(
            summary = "Update a guide",
            description = "Updates an existing guide's descriptive information and options. Administrator access required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator access required"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{guideId}")
    public ResponseEntity<@NonNull GuideResponseDTO> updateGuide(
            @PathVariable Long guideId,
            @RequestBody @Valid GuideUpdateRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(
                guideService.updateGuide(guideId, requestDTO)
        );
    }

    @Operation(
            summary = "Delete a guide",
            description = "Deletes a guide and its associated days, activities and invitations. Administrator access required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Guide deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator access required"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{guideId}")
    public ResponseEntity<@NonNull Void> deleteGuide(
            @PathVariable Long guideId
    ) {
        guideService.deleteGuide(guideId);

        return ResponseEntity.noContent().build();
    }

    // extracts the immutable database ID from the JWT subject
    private Long getAuthenticatedUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

}