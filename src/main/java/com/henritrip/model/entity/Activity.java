package com.henritrip.model.entity;

import com.henritrip.model.enums.ActivityCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Entity representing an activity within a guide day.
 * Contains activity details, location information, and its visit order.
 * Tracks creation and update timestamps automatically.
 */
@Entity
@Table(name = "activities")
@Getter
@Setter
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guide_day_id", nullable = false)
    private GuideDay guideDay;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "category", nullable = false, columnDefinition = "activity_category")
    private ActivityCategory category;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "website")
    private String website;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

}
