package com.henritrip.repository;

import com.henritrip.model.entity.GuideInvitation;
import com.henritrip.model.entity.GuideInvitationId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for GuideInvitation entity, providing methods
 * to manage guide invitations and verify user access.
 */
@Repository
public interface GuideInvitationRepository extends
        JpaRepository<@NonNull GuideInvitation, @NonNull GuideInvitationId> {

    // check whether a user has been invited to a specific guide
    boolean existsByIdGuideIdAndIdUserId(Long guideId, Long userId);

    // fetch all invitations associated with a specific guide
    List<GuideInvitation> findByGuideId(Long guideId);

    // fetch all invitations associated with a specific user
    List<GuideInvitation> findByUserId(Long userId);

}
