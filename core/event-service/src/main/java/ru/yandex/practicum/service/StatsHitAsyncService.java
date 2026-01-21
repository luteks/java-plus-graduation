package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dto.HitDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsHitAsyncService {

    private final StatsClient statsClient;

    @Async
    public void sendHitAsync(HitDto hitDto) {
        try {
            statsClient.create(hitDto);
        } catch (Exception e) {
            log.warn("Не удалось отправить хит в stats-service: {}", e.getMessage());
        }
    }
}