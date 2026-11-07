
package com.henritrip.model.entity;

import com.henritrip.model.enums.AudienceOption;
import com.henritrip.model.enums.SeasonOption;
import com.henritrip.model.enums.TransportOption;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a travel guide in the system.
 * Contains guide information, available options, associated days, creator, and invited users.
 * Tracks creation and update timestamps automatically.
 */
@Entity
@Table(name = "guides")
@Getter
@Setter
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ElementCollection(targetClass = TransportOption.class)
    @CollectionTable(
            name = "guide_transports",
            joinColumns = @JoinColumn(name = "guide_id")
    )
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transport", nullable = false, columnDefinition = "transport_option")
    private Set<TransportOption> transports = new HashSet<>();

    @ElementCollection(targetClass = SeasonOption.class)
    @CollectionTable(
            name = "guide_seasons",
            joinColumns = @JoinColumn(name = "guide_id")
    )
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "season", nullable = false, columnDefinition = "season_option")
    private Set<SeasonOption> seasons = new HashSet<>();

    @ElementCollection(targetClass = AudienceOption.class)
    @CollectionTable(
            name = "guide_audiences",
            joinColumns = @JoinColumn(name = "guide_id")
    )
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "audience", nullable = false, columnDefinition = "audience_option")
    private Set<AudienceOption> audiences = new HashSet<>();

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuideDay> days = new ArrayList<>();

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuideInvitation> invitations = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

}
