package ru.yandex.practicum.ewm.controller;

import com.google.protobuf.Empty;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.ewm.service.CollectorService;
import ru.yandex.practicum.ewm.stats.messages.UserActionProto;
import ru.yandex.practicum.ewm.stats.services.UserActionControllerGrpc;

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final CollectorService collectorService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        long start = System.currentTimeMillis();
        try {

            if (request.getActionType() == null || request.getActionType().isBlank()) {
                log.warn("Некорректный actionType в UserActionProto: {}", request);
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("actionType must not be empty")
                        .asRuntimeException());
                return;
            }


            Metadata metadata = Context.current().get(Metadata.KEY);


            log.info("[{}] Новое действие UserActionProto: {}", Instant.now(), request);


            collectorService.collectUserAction(request);

            long elapsed = System.currentTimeMillis() - start;
            log.info("Действие успешно обработано за {} ms", elapsed);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            log.error("Валидационная ошибка для запроса {}: {}", request, ex.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(ex.getLocalizedMessage())
                    .withCause(ex)
                    .asRuntimeException());
        } catch (StatusRuntimeException sre) {

            log.error("gRPC статус-ошибка: {}", sre.getStatus(), sre);
            responseObserver.onError(sre);
        } catch (Exception e) {
            log.error("Ошибка при получении нового действия: {}, ошибка: {}", request, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getLocalizedMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}