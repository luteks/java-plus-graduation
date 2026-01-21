package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.EventState;
import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String annotation;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private EventCategory category;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @Column(name = "event_date_time", nullable = false)
    private Instant eventDateTime;

    @Column(name = "published_on")
    private Instant publishedOn;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit = 0;

    @Column(name = "is_moderated")
    private Boolean isModerated = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state;

    // КЛЮЧЕВОЕ ПОЛЕ — ОБЯЗАТЕЛЬНО ИНИЦИАЛИЗИРУЙТЕ!
    @Column(name = "confirmed_requests", nullable = false)
    private Long confirmedRequests = 0L;
}