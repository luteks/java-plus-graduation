package ru.yandex.practicum.ewm.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.model.EventSimilarity;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Component
public class EventSimilarityMapper {
    public EventSimilarity mapToEventSimilarity(EventSimilarityAvro avro) {
        EventSimilarity eventSimilarity = new EventSimilarity();
        eventSimilarity.setEventA(avro.getEventA());
        eventSimilarity.setEventB(avro.getEventB());
        eventSimilarity.setScore(avro.getScore());
        eventSimilarity.setTimestamp(avro.getTimestamp());
        log.info("Результат: {}", eventSimilarity);
        return eventSimilarity;
    }
}