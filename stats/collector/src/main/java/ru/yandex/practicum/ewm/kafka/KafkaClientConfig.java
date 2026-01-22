package ru.yandex.practicum.ewm.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

@Slf4j
@Configuration
public class KafkaClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "kafka.producer.properties")
    public Properties kafkaProducerProperties() {
        return new Properties();
    }

    @Bean
    Producer<Long, SpecificRecordBase> kafkaProducer(Properties kafkaProducerProperties) {
        log.info("Создаём KafkaProducer с {} параметрами: {}",
                kafkaProducerProperties.size(), kafkaProducerProperties.keySet());
        return new KafkaProducer<>(kafkaProducerProperties);
    }

    @Bean
    KafkaClient getKafkaClient(Producer<Long, SpecificRecordBase> kafkaProducer) {
        return new KafkaClient() {
            @Override
            public void send(String topic, Instant timestamp, Long eventId, SpecificRecordBase event) {
                long start = System.currentTimeMillis();
                ProducerRecord<Long, SpecificRecordBase> record =
                        new ProducerRecord<>(topic, null, timestamp.toEpochMilli(), eventId, event);
                log.info("Готовим отправку в Kafka | topic='{}', eventId={}, timestamp={}, schema={}, partition=null",
                        topic, eventId, timestamp, event != null ? event.getSchema().getName() : "null");
                try {
                    kafkaProducer.send(record, (metadata, exception) -> {
                        if (exception != null) {
                            log.error("Ошибка при отправке сообщения в Kafka | topic={}, eventId={}, ошибка: {}",
                                    topic, eventId, exception.getMessage(), exception);
                        } else {
                            log.info("Сообщение успешно отправлено | topic={}, partition={}, offset={}, eventId={}, elapsedMs={}",
                                    metadata.topic(), metadata.partition(), metadata.offset(), eventId,
                                    (System.currentTimeMillis() - start));
                        }
                    });


                } catch (Exception ex) {
                    log.error("Сбой отправки в Kafka | topic={}, eventId={}, ошибка: {}", topic, eventId, ex.getMessage(), ex);
                }
            }

            @Override
            public void close() {
                log.info("Флашим продюсер и закрываем соединение с Kafka...");
                kafkaProducer.flush();
                kafkaProducer.close(Duration.ofSeconds(10));
                log.info("KafkaProducer закрыт успешно");
            }
        };
    }
}