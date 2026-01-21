package ru.yandex.practicum.ewm.service;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;

public interface AggregatorService {
    List<EventSimilarityAvro> aggregationUserAction(SpecificRecordBase userActionAvro);
}