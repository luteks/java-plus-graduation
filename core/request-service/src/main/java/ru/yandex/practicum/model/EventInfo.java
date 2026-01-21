package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "event_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventInfo {
    @Id
    private Long id;
    private Long ownerId;
    private Boolean isModerated;
    private Integer participantLimit;
    private Instant publishedOn;
}