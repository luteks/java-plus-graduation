package ru.yandex.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.request.dto.RequestDto;

import java.util.List;

public interface RequestServiceClient {
    @GetMapping("/{userId}/requests/{eventId}")
    List<RequestDto> getRequestsForUserEvent(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @GetMapping("/{userId}/events/requests/all")
    List<RequestDto> findAllByEventIdIn(@PathVariable Long userId, @RequestParam List<Long> eventIds) throws FeignException;
}