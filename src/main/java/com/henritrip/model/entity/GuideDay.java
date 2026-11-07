package com.henritrip.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a day within a travel guide.
 * Contains the day's activities in their intended visit order.
 * Tracks creation and update timestamps automatically.
 */
@Entity
@Table(name = "guide_days")
@Getter
@Setter
public class GuideDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @OneToMany(
            mappedBy = "guideDay",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Activity> activities = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

}
