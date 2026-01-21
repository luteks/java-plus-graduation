package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.event.EventShortForRequestDto;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.service.EventService;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
@Slf4j
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventShortForRequestDto getById(@PathVariable Long eventId) {
        log.info(">>> InternalEventController called for eventId={}", eventId);
        Event event = eventService.getEventIfExist(eventId);
        return EventShortForRequestDto.builder()
                .id(event.getId())
                .ownerId(event.getOwnerId())
                .publishedOn(event.getPublishedOn())
                .isModerated(event.getIsModerated())
                .participantLimit(event.getParticipantLimit())
                .build();
    }
}