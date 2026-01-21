package ru.yandex.practicum.client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.ewm.stats.messages.ActionTypeProto;
import ru.yandex.practicum.ewm.stats.messages.UserActionProto;
import ru.yandex.practicum.ewm.stats.services.UserActionControllerGrpc;

import java.time.Instant;

@Slf4j
@Component
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    public void sendView(Long userId, Long eventId) {
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();
        log.info("Передаем просмотр от пользователя: {}", request);
        collectorClient.collectUserAction(request);
    }

    public void sendLike(Long userId, Long eventId) {
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_LIKE)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();
        log.info("Передаем лайк от пользователя: {}", request);
        collectorClient.collectUserAction(request);
    }

    public void sendRegistration(Long userId, Long eventId) {
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_REGISTER)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();
        log.info("Передаем регистрацию от пользователя: {}", request);
        collectorClient.collectUserAction(request);
    }
}