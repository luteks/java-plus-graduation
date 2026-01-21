package ru.yandex.practicum.ewm.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.ewm.handler.RecommendationsHandler;
import ru.yandex.practicum.ewm.stats.messages.InteractionsCountRequestProto;
import ru.yandex.practicum.ewm.stats.messages.RecommendedEventProto;
import ru.yandex.practicum.ewm.stats.messages.SimilarEventsRequestProto;
import ru.yandex.practicum.ewm.stats.messages.UserPredictionsRequestProto;
import ru.yandex.practicum.ewm.stats.services.RecommendationsControllerGrpc;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationsHandler handler;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Запрос получения рекомендаций : {}", request);
            handler.getRecommendationsForUser(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка получения рекомендаций : {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Запрос получения  похожих мероприятий: {}", request);
            handler.getSimilarEvents(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка получения  похожих мероприятий: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Запрос суммы весов для события: {}", request);
            handler.getInteractionsCount(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка суммы весов для событий: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}