package ru.yandex.practicum.dto.event;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShortForRequestDto {
    private Long id;
    private Initiator initiator;
    private Instant publishedOn;
    private Boolean isModerated;
    private Integer participantLimit;

    @Getter @Setter
    public static class Initiator {
        private Long id;
    }

    public Long getOwnerId() {
        return initiator != null ? initiator.id : null;
    }
}