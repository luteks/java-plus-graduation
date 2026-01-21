package ru.yandex.practicum.stats.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.StatsDto;
import ru.yandex.practicum.exception.model.BadRequestException;
import ru.yandex.practicum.stats.mapper.HitMapper;
import ru.yandex.practicum.stats.model.App;
import ru.yandex.practicum.stats.model.Hit;
import ru.yandex.practicum.stats.repository.AppRepository;
import ru.yandex.practicum.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final HitRepository hitRepository;
    private final AppRepository appRepository;

    @Override
    public List<StatsDto> getStats(LocalDateTime startRange, LocalDateTime endRange, List<String> uris, boolean unique) {
        if (startRange == null || endRange == null || startRange.isAfter(endRange) || startRange.equals(endRange)) {
            throw new BadRequestException("Неверный диапазон дат для выгрузки статистики");
        }

        List<Object[]> rawData;
        if (uris == null || uris.isEmpty()) {
            rawData = unique
                    ? hitRepository.findAllUniqueHitsRaw(startRange, endRange)
                    : hitRepository.findAllHitsRaw(startRange, endRange);
        } else {
            rawData = unique
                    ? hitRepository.findUniqueHitsByUrisRaw(startRange, endRange, uris)
                    : hitRepository.findHitsByUrisRaw(startRange, endRange, uris);
        }

        return rawData.stream()
                .map(row -> unique
                        ? new StatsDto((String) row[0], (String) row[1])
                        : new StatsDto((String) row[0], (String) row[1], ((Number) row[2]).longValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void hit(ru.yandex.practicum.dto.HitDto hitDto) {
        Hit hit = HitMapper.toHit(hitDto);
        hit.setTimestamp(LocalDateTime.now());
        Optional<App> existedApp = appRepository.findByName(hitDto.getApp());
        if (existedApp.isPresent()) {
            hit.setApp(existedApp.get());
        } else {
            App app = new App();
            app.setName(hitDto.getApp());
            hit.setApp(appRepository.save(app));
            log.info("В таблицу APP добавлено новое приложение - {}", app.getName());
        }
        hitRepository.save(hit);
        log.info("В таблицу HIT добавлено обращение к {} с IP {} от источника {}", hit.getUri(), hit.getIp(),
                hit.getApp().getName());
    }
}