package com.henritrip.controller;

import com.henritrip.dto.guideinvitation.GuideInvitationResponseDTO;
import com.henritrip.service.GuideInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller responsible for managing guide invitations.
 */
@Tag(
        name = "Guide Invitations",
        description = "Guide invitation management endpoints"
)
@SecurityRequirement(name = "Authorization")
@RestController
@RequestMapping("/api/guides/{guideId}/invitations")
@PreAuthorize("hasRole('ADMIN')")
public class GuideInvitationController {

    private final GuideInvitationService guideInvitationService;

    public GuideInvitationController(GuideInvitationService guideInvitationService) {
        this.guideInvitationService = guideInvitationService;
    }

    @Operation(
            summary = "Invite a user to a guide",
            description = "Grants a user access to a guide. Administrator access required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User invited successfully"),
            @ApiResponse(responseCode = "403", description = "Administrator access required"),
            @ApiResponse(responseCode = "404", description = "Guide or user not found"),
            @ApiResponse(responseCode = "409", description = "User is already invited")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<@NonNull GuideInvitationResponseDTO> inviteUser(
            @PathVariable Long guideId,
            @PathVariable Long userId,
            Authentication authentication
    ) {
        Long invitedById = getAuthenticatedUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guideInvitationService.inviteUser(guideId, userId, invitedById));
    }

    @Operation(
            summary = "Retrieve guide invitations",
            description = "Returns all users invited to a guide. Administrator access required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitations retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Administrator access required"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    @GetMapping
    public ResponseEntity<@NonNull List<GuideInvitationResponseDTO>>
    getGuideInvitations(
            @PathVariable Long guideId
    ) {
        return ResponseEntity.ok(guideInvitationService.getGuideInvitations(guideId));
    }

    @Operation(
            summary = "Remove a guide invitation",
            description = "Revokes a user's access to a guide. Administrator access required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Invitation removed successfully"),
            @ApiResponse(responseCode = "403", description = "Administrator access required"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<@NonNull Void> removeInvitation(
            @PathVariable Long guideId,
            @PathVariable Long userId
    ) {
        guideInvitationService.removeInvitation(guideId, userId);

        return ResponseEntity.noContent().build();
    }

    // extracts the authenticated user's ID from the JWT subject
    private Long getAuthenticatedUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

}
