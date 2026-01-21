package ru.yandex.practicum.ewm.handler;

import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventSimilarityHandler {
    void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro);
}