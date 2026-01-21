package ru.yandex.practicum.ewm.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.mapper.EventSimilarityMapper;
import ru.yandex.practicum.ewm.model.EventSimilarity;
import ru.yandex.practicum.ewm.repository.EventSimilarityRepository;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityHandlerImpl implements EventSimilarityHandler {
    private final EventSimilarityRepository similarityRepository;
    private final EventSimilarityMapper similarityMapper;

    @Override
    public void handleEventSimilarity(EventSimilarityAvro avro) {
        EventSimilarity similarity = similarityMapper.mapToEventSimilarity(avro);
        if (!similarityRepository.existsByEventAAndEventB(similarity.getEventA(), similarity.getEventB())) {
            similarity = similarityRepository.save(similarity);
            log.info("Сохранить новое сходство: {}", similarity);
        } else {
            EventSimilarity oldSimilarity = similarityRepository
                    .findByEventAAndEventB(similarity.getEventA(), similarity.getEventB()).get();
            log.info("Найти в БД старое сходство: {}", oldSimilarity);
            if (similarity.getScore() > oldSimilarity.getScore()) {
                oldSimilarity.setScore(similarity.getScore());
                oldSimilarity.setTimestamp(similarity.getTimestamp());
                oldSimilarity = similarityRepository.save(oldSimilarity);
                log.info("Сходство увеличилось, обновить БД: {}", oldSimilarity);
            } else {
                log.info("Сходство не увеличилось, не обновлять");
            }
        }
    }
}