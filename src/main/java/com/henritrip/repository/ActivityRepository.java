package com.henritrip.repository;

import com.henritrip.model.entity.Activity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Activity entity, providing methods
 * to retrieve activities by guide day and visit order.
 */
@Repository
public interface ActivityRepository extends JpaRepository<@NonNull Activity, @NonNull Long> {

    // fetch all activities associated with a specific guide day, ordered by visit order
    List<Activity> findByGuideDayIdOrderByVisitOrderAsc(Long guideDayId);

    // fetch a specific activity belonging to a specific guide day
    Optional<Activity> findByIdAndGuideDayId(Long id, Long guideDayId);

    // check whether a visit order already exists within a guide day
    boolean existsByGuideDayIdAndVisitOrder(Long guideDayId, Integer visitOrder);

    // check whether another activity already uses the given visit order within a guide day
    boolean existsByGuideDayIdAndVisitOrderAndIdNot(
            Long guideDayId, Integer visitOrder, Long activityId
    );

}
