package ru.yandex.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.ewm.kafka.KafkaClient;
import ru.yandex.practicum.ewm.mapper.UserActionMapper;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.ewm.stats.messages.UserActionProto;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    private final KafkaClient kafkaClient;
    private final UserActionMapper userActionMapper;

    @Value("${kafka.topics.user-actions}")
    private String userActionTopic;

    @Override
    public void collectUserAction(UserActionProto request) {
        UserActionAvro actionAvro = userActionMapper.mapToAvro(request);
        log.info("Отправляем в топик:  {}, действие пользователя:  {}", userActionTopic, actionAvro);
        kafkaClient.send(
                userActionTopic,
                actionAvro.getTimestamp(),
                actionAvro.getEventId(),
                actionAvro);
    }
}