package ru.yandex.practicum.feign.event;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.event.EventShortForRequestDto;
import ru.yandex.practicum.exception.NotFoundException;

@Component
public class EventClientFallback implements EventClient {

    @Override
    public EventShortForRequestDto getById(Long eventId) {
        throw new NotFoundException("Событие с id=" + eventId + " недоступно");
    }
}