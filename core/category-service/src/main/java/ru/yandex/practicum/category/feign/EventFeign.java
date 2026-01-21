package ru.yandex.practicum.category.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.EventFullDto;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventFeign {
    @GetMapping("/admin/events/check/category")
    List<EventFullDto> adminGetAllEventsByCategory(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);
}