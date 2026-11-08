package com.henritrip.repository;

import com.henritrip.model.entity.Guide;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Guide entity, providing methods
 * to retrieve guides and check user access.
 */
@Repository
public interface GuideRepository extends JpaRepository<@NonNull Guide, @NonNull Long> {

    // fetch paginated guides to which a specific user has been invited
    Page<@NonNull Guide> findDistinctByInvitationsUserId(Long userId, Pageable pageable);

    // check whether a user has access to a specific guide
    boolean existsByIdAndInvitationsUserId(Long guideId, Long userId);

}
