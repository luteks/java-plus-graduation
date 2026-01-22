package ru.yandex.practicum.ewm.handler;

import ru.yandex.practicum.ewm.stats.messages.InteractionsCountRequestProto;
import ru.yandex.practicum.ewm.stats.messages.RecommendedEventProto;
import ru.yandex.practicum.ewm.stats.messages.SimilarEventsRequestProto;
import ru.yandex.practicum.ewm.stats.messages.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationsHandler {

    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}