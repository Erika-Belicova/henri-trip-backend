package com.henritrip.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite identifier for a guide invitation.
 * A guide and a user together uniquely identify an invitation.
 */
@Embeddable
@Getter
@Setter
public class GuideInvitationId implements Serializable {

    @Column(name = "guide_id")
    private Long guideId;

    @Column(name = "user_id")
    private Long userId;

    public GuideInvitationId() {
    }

    public GuideInvitationId(Long guideId, Long userId) {
        this.guideId = guideId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GuideInvitationId that)) return false;

        return Objects.equals(guideId, that.guideId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guideId, userId);
    }
}
