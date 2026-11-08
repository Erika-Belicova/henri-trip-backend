package com.henritrip.repository;

import com.henritrip.model.entity.GuideDay;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GuideDay entity, providing methods
 * to retrieve guide days by guide ID and day number.
 */
@Repository
public interface GuideDayRepository extends JpaRepository<@NonNull GuideDay, @NonNull Long> {

    // fetch all days associated with a specific guide, ordered by day number
    List<GuideDay> findByGuideIdOrderByDayNumberAsc(Long guideId);

    // fetch a specific day belonging to a specific guide
    Optional<GuideDay> findByIdAndGuideId(Long id, Long guideId);

}
