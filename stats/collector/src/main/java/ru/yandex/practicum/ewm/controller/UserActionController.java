package ru.yandex.practicum.ewm.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.ewm.service.CollectorService;
import ru.yandex.practicum.ewm.stats.messages.UserActionProto;
import ru.yandex.practicum.ewm.stats.services.UserActionControllerGrpc;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final CollectorService collectorService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Новое действие UserActionProto: {}", request);
            collectorService.collectUserAction(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        }catch (Exception e) {
            log.error("Ошибка при получении нового действия: {}, ошибка: {}", request, e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}