package ru.yandex.practicum.event.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.EventFullDto;
import ru.yandex.practicum.event.dto.AdminEventParams;
import ru.yandex.practicum.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.yandex.practicum.dto.Constants.FORMAT_DATETIME;


@Slf4j
@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> adminGetAllEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = FORMAT_DATETIME) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        AdminEventParams adminEventParams = new AdminEventParams(users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getAllEvents(adminEventParams);
    }

    @GetMapping("/check/category")
    public List<EventFullDto> adminGetAllEventsByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Административный запрос на поиск событий по категории: {}", categoryId);
        return eventService.findAllByCategoryId(categoryId, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto adminUpdateEvent(@PathVariable("eventId") long eventId,
                                         @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Запрос на обновление админом события с id: {}, {}", eventId, updateEventAdminRequest);
        return eventService.update(eventId, updateEventAdminRequest);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findEventById(@PathVariable(name = "eventId") Long eventId) {
        log.info("Запрос на поиск любого события по id: {}", eventId);
        return eventService.findEventById(eventId);
    }
}