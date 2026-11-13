package com.henritrip.dto.guideinvitation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * DTO representing a guide invitation.
 */
@Data
@Schema(description = "Guide invitation information")
public class GuideInvitationResponseDTO {

    @Schema(description = "ID of the guide")
    private Long guideId;

    @Schema(description = "ID of the invited user")
    private Long userId;

    @Schema(description = "Email of the invited user")
    private String userEmail;

    @Schema(description = "ID of the user who created the invitation")
    private Long invitedById;

    @Schema(description = "Invitation creation timestamp")
    private OffsetDateTime createdAt;

}
