package ru.yandex.practicum.ewm.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.handler.UserActionHandler;
import ru.yandex.practicum.ewm.kafka.KafkaClient;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserActionProcessor {
    private final Consumer<Long, UserActionAvro> userActionConsumer;
    private final UserActionHandler userActionHandler;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);


    @Value("${analyzer.kafka.topics.user-actions}")
    private String userActionsTopic;

    public UserActionProcessor(KafkaClient kafkaClient, UserActionHandler userActionHandler) {
        this.userActionConsumer = kafkaClient.getKafkaUserActionConsumer();
        this.userActionHandler = userActionHandler;
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(userActionConsumer::wakeup));
        try {
            userActionConsumer.subscribe(List.of(userActionsTopic));
            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = userActionConsumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<Long, UserActionAvro> record : records) {
                        UserActionAvro avro = record.value();

                        log.info("{}: отправка сообщения в handler", ru.yandex.practicum.ewm.processor.UserActionProcessor.class.getSimpleName());
                        userActionHandler.handleUserAction(avro);

                        manageOffsets(record, count, userActionConsumer);
                        count++;
                    }
                    userActionConsumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("{}: Ошибка обработки userAction", ru.yandex.practicum.ewm.processor.UserActionProcessor.class.getSimpleName(), e);
        } finally {
            try {
                userActionConsumer.commitSync();
            } finally {
                log.info("{}: Закрыть консьюмер", ru.yandex.practicum.ewm.processor.UserActionProcessor.class.getSimpleName());
                userActionConsumer.close();
            }
        }
    }

    private static void manageOffsets(
            ConsumerRecord<Long, UserActionAvro> record,
            int count,
            Consumer<Long, UserActionAvro> consumer
    ) {

        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}