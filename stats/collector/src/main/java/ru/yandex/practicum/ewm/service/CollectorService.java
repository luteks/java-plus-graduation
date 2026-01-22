package ru.yandex.practicum.ewm.service;

import ru.yandex.practicum.ewm.stats.messages.UserActionProto;

public interface CollectorService {
    void collectUserAction(UserActionProto request);
}