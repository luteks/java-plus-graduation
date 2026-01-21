package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.event.EventShortForRequestDto;
import ru.yandex.practicum.dto.request.RequestDto;
import ru.yandex.practicum.dto.request.RequestStatusUpdateRequest;
import ru.yandex.practicum.dto.request.RequestStatusUpdateResponse;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.enums.RequestStatus;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.ForbiddenException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.feign.event.EventClient;
import ru.yandex.practicum.feign.request.RequestClient;
import ru.yandex.practicum.feign.request.RequestClientSingle;
import ru.yandex.practicum.feign.user.UserClient;
import ru.yandex.practicum.mapper.RequestMapper;
import ru.yandex.practicum.model.EventInfo;
import ru.yandex.practicum.model.ParticipationRequest;
import ru.yandex.practicum.repository.EventInfoRepository;
import ru.yandex.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.enums.RequestStatus.CONFIRMED;
import static ru.yandex.practicum.enums.RequestStatus.REJECTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestClient requestClient;
    private final RequestClientSingle requestClientSingle;
    private final EventInfoRepository eventInfoRepository;

    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        UserShortDto user;
        try {
            user = userClient.getById(userId);
        } catch (Exception e) {
            log.error("Ошибка при получении пользователя с id {}: {}", userId, e.getMessage());
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        EventShortForRequestDto event;
        try {
            event = eventClient.getById(eventId);
        } catch (Exception e) {
            log.error("Ошибка при получении события с id {}: {}", eventId, e.getMessage());
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }

        if (event == null) {
            throw new NotFoundException("Событие не найдено или недоступно");
        }

        if (event.getOwnerId() == null) {
            throw new NotFoundException("Событие не имеет владельца");
        }

        if (!Objects.equals(event.getOwnerId(), userId)) {
            throw new ForbiddenException("User с id " + userId + " не владелец события " + eventId);
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::fromRequestToRequestDto)
                .toList();
    }

    public RequestStatusUpdateResponse updateRequest(Long userId, Long eventId, RequestStatusUpdateRequest requestDto) {
        log.info("START updateRequest for userId: {}, eventId: {}", userId, eventId);

        // Валидация
        if (requestDto == null || requestDto.getRequestIds() == null || requestDto.getStatus() == null) {
            throw new ConflictException("Некорректные данные для обновления заявки");
        }

        // Получаем пользователя (можно тоже кэшировать, но оставим Feign для user)
        UserShortDto user = userClient.getById(userId);
        if (user == null) throw new NotFoundException("Пользователь не найден");

        // Получаем данные события ЛОКАЛЬНО
        EventInfo event = eventInfoRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!Objects.equals(user.getId(), event.getOwnerId())) {
            throw new ForbiddenException("User с id " + userId + " не владелец события " + eventId);
        }

        // Проверка модерации и лимита
        if (!event.getIsModerated() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Запрос составлен некорректно.");
        }

        // Получаем заявки
        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(requestDto.getRequestIds());
        if (requests.isEmpty()) {
            throw new ConflictException("Заявки с указанными ID не найдены");
        }

        // Проверяем, что все заявки относятся к этому событию и в статусе PENDING
        for (ParticipationRequest r : requests) {
            if (!Objects.equals(r.getEventId(), eventId)) {
                throw new ConflictException("Заявка не относится к событию");
            }
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус можно изменить только у заявок в состоянии ожидания");
            }
        }

        // Считаем подтверждённые
        Long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников достигнут");
        }

        // Обновляем статусы
        Set<RequestDto> confirmed = new HashSet<>();
        Set<RequestDto> rejected = new HashSet<>();

        List<ParticipationRequest> toSave = new ArrayList<>();
        for (ParticipationRequest req : requests) {
            if (requestDto.getStatus() == RequestStatus.CONFIRMED) {
                if (confirmedCount < event.getParticipantLimit()) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(RequestMapper.fromRequestToRequestDto(req));
                    confirmedCount++;
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(RequestMapper.fromRequestToRequestDto(req));
                }
            } else if (requestDto.getStatus() == RequestStatus.REJECTED) {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.fromRequestToRequestDto(req));
            }
            toSave.add(req);
        }

        requestRepository.saveAll(toSave);
        return new RequestStatusUpdateResponse(confirmed, rejected);
    }

    public List<RequestDto> getByUserId(Long userId) {
        try {
            userClient.getById(userId);
        } catch (Exception e) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::fromRequestToRequestDto)
                .toList();
    }

    public RequestDto create(Long userId, Long eventId) {
        UserShortDto user;
        EventShortForRequestDto event;
        try {
            user = userClient.getById(userId);
            event = eventClient.getById(eventId);
        } catch (Exception e) {
            log.error("Ошибка Feign клиента при создании заявки: {}", e.getMessage());
            throw new NotFoundException("Не удалось найти пользователя или событие");
        }
        eventInfoRepository.save(
                EventInfo.builder()
                        .id(eventId)
                        .ownerId(event.getOwnerId())
                        .isModerated(event.getIsModerated())
                        .participantLimit(event.getParticipantLimit())
                        .publishedOn(event.getPublishedOn())
                        .build()
        );
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (event == null) {
            throw new NotFoundException("Событие не найдено");
        }
        if (event.getOwnerId() == null) {
            throw new NotFoundException("Событие не имеет владельца");
        }

        if (Objects.equals(user.getId(), event.getOwnerId())) {
            throw new ConflictException("Инициатор не может подать заявку на своё событие");
        }
        if (event.getPublishedOn() == null) {
            throw new ConflictException("Событие не опубликовано");
        }

        Long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (confirmedCount >= event.getParticipantLimit() && event.getParticipantLimit() > 0) {
            throw new ConflictException("Лимит участников достигнут");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Заявка уже существует");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .createdOn(LocalDateTime.now())
                .eventId(eventId)
                .requesterId(userId)
                .status(event.getParticipantLimit() == 0 || !event.getIsModerated() ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .build();

        ParticipationRequest saved = requestRepository.save(request);
        return RequestMapper.fromRequestToRequestDto(saved);
    }

    public RequestDto cancelRequestByUser(Long userId, Long requestId) {
        try {
            userClient.getById(userId);
        } catch (Exception e) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        if (!Objects.equals(request.getRequesterId(), userId)) {
            throw new ConflictException("Можно отменить только свою заявку");
        }

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.fromRequestToRequestDto(requestRepository.save(request));
    }

    public Map<Long, Long> getConfirmedRequestsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> map = requestClient.getConfirmedCounts(eventIds);
        return map != null ? map : Collections.emptyMap();
    }

    public Long getCountByStatus(Long eventId, RequestStatus status) {
        try {
            return requestClientSingle.getCountByStatus(eventId, status);
        } catch (Exception e) {
            log.error("Ошибка при получении количества заявок для события {}: {}", eventId, e.getMessage(), e);
            return 0L;
        }
    }
}