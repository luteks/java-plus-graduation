package ru.yandex.practicum.ewm.handler;

import ru.yandex.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionHandler {
    void handleUserAction(UserActionAvro userActionAvro);
}