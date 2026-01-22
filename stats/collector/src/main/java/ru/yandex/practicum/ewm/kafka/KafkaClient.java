package ru.yandex.practicum.ewm.kafka;

import org.apache.avro.specific.SpecificRecordBase;

import java.time.Instant;

public interface KafkaClient extends AutoCloseable {
    void send(String topic, Instant timestamp, Long eventId, SpecificRecordBase event);
}