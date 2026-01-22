package ru.yandex.practicum.event.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.CollectorClient;
import ru.yandex.practicum.dto.EventFullDto;
import ru.yandex.practicum.dto.EventShortDto;
import ru.yandex.practicum.event.dto.ReqParam;
import ru.yandex.practicum.event.model.EventSort;
import ru.yandex.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.yandex.practicum.dto.Constants.FORMAT_DATETIME;


@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;
    private final CollectorClient collectorClient;

    @GetMapping
    public List<EventShortDto> publicGetAllEvents(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeStart,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(required = false) EventSort sort,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  HttpServletRequest request) {
        ReqParam reqParam = ReqParam.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        log.info("Публичный запрос на поиск событий по параметрам: {}", reqParam);
        List<EventShortDto> events = eventService.getAllEvents(reqParam);
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto publicGetEvent(@PathVariable long id, @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("Публичный запрос на поиск события: {}", id);
        EventFullDto eventFullDto = eventService.publicGetEvent(id);
        collectorClient.sendView(userId, id);
        return eventFullDto;
    }

    @GetMapping("/recommendations")
    public List<EventFullDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId, @RequestParam int maxResults) {
        return eventService.getRecommendations(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    public void likeEvent(@PathVariable long eventId, @RequestHeader("X-EWM-USER-ID") long userId) {
        eventService.likeEvent(userId, eventId);
    }
}