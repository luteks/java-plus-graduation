package ru.yandex.practicum.ewm.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.mapper.UserActionMapper;
import ru.yandex.practicum.ewm.model.UserAction;
import ru.yandex.practicum.ewm.repository.UserActionRepository;
import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler {

    private final UserActionRepository actionRepository;
    private final UserActionMapper actionMapper;

    @Override
    public void handleUserAction(UserActionAvro avro) {
        UserAction action = actionMapper.mapToUserAction(avro);

        if (!actionRepository.existsByEventIdAndUserId(action.getEventId(), action.getUserId())) {
            action = actionRepository.save(action);
            log.info("Сохранить новое действие: {}", action);
        } else {
            UserAction oldAction = actionRepository
                    .findByEventIdAndUserId(action.getEventId(), action.getUserId()).get();
            log.info("Найти в БД старое действие: {}", oldAction);
            if (action.getWeight() > oldAction.getWeight()) {
                oldAction.setWeight(action.getWeight());
                oldAction.setTimestamp(action.getTimestamp());
                oldAction = actionRepository.save(oldAction);
                log.info("Вес действия увеличился, обновить БД: {}", oldAction);
            } else {
                log.info("Вес действия не увеличился, не обновлять");
            }
        }
    }
}