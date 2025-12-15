package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.event.dto.EventDto;

import java.util.List;

public interface EventServiceClient {
    @GetMapping("/admin/events/{eventId}/full")
    EventDto getEventFullById(@PathVariable long eventId) throws FeignException;

    @GetMapping("/users/{userId}/events/{eventId}/optional")
    EventDto getEventByIdAndInitiatorId(@PathVariable long userId, @PathVariable long eventId) throws FeignException;

    @GetMapping("/users/{userId}/events/initiator")
    List<EventDto> getAllEventByInitiatorId(@PathVariable long userId) throws FeignException;
}