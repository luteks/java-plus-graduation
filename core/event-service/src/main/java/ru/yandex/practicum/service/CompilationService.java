package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.dto.compilation.CompilationDto;
import ru.yandex.practicum.dto.compilation.CompilationRequestDto;
import ru.yandex.practicum.dto.event.EventShortDto;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.exception.BadRequestException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.feign.request.RequestClient;
import ru.yandex.practicum.feign.user.UserClient;
import ru.yandex.practicum.mapper.CompilationMapper;
import ru.yandex.practicum.mapper.EventCategoryMapper;
import ru.yandex.practicum.mapper.EventMapper;
import ru.yandex.practicum.model.Compilation;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.repository.CompilationRepository;
import ru.yandex.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final RequestClient requestClient; // ← ВОССТАНОВЛЕНО
    private final StatsClient statClient;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<CompilationDto> getAll(boolean pinned, int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = compilationRepository.getAllByPinned(pinned, pageable).getContent();

        return compilations.stream()
                .map(compilation -> {
                    Set<EventShortDto> items = getEventsShorts(compilation.getEvents());
                    return CompilationMapper.toDtoFromCompilation(compilation, items);
                })
                .collect(Collectors.toList());
    }

    public CompilationDto getById(long id) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + id + " не найдена"));

        Set<EventShortDto> items = getEventsShorts(compilation.getEvents());
        return CompilationMapper.toDtoFromCompilation(compilation, items);
    }

    public CompilationDto create(CompilationRequestDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Title не может быть пустым");
        }

        Set<Event> events = new HashSet<>();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
            if (events.size() != dto.getEvents().size()) {
                throw new NotFoundException("Некоторые события не найдены");
            }
        }

        Compilation compilation = CompilationMapper.toCompilationFromDto(dto, events);
        if (dto.getPinned() == null) {
            compilation.setPinned(false);
        }

        Compilation saved = compilationRepository.save(compilation);
        Set<EventShortDto> items = getEventsShorts(saved.getEvents());
        return CompilationMapper.toDtoFromCompilation(saved, items);
    }

    public void delete(long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compilationId + " не существует"));
        compilationRepository.delete(compilation);
    }

    public CompilationDto updateCompilation(long compilationId, CompilationRequestDto dto) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compilationId + " не существует"));

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
            if (events.size() != dto.getEvents().size()) {
                throw new NotFoundException("Некоторые события не найдены");
            }
            compilation.setEvents(events);
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            compilation.setTitle(dto.getTitle());
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        Compilation saved = compilationRepository.save(compilation);
        Set<EventShortDto> items = getEventsShorts(saved.getEvents());
        return CompilationMapper.toDtoFromCompilation(saved, items);
    }

    public Map<Long, Integer> getEventsViewsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashMap<>();
        }

        List<String> uris = eventIds.stream().map(id -> "/events/" + id).toList();

        try {
            ResponseEntity<Object> response = statClient.getStats(
                    "2000-01-01 00:00:00",
                    LocalDateTime.now().format(formatter),
                    uris,
                    false
            );

            Map<Long, Integer> viewsMap = new HashMap<>();
            if (response.getBody() instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> map) {
                        String uri = (String) map.get("uri");
                        if (uri != null && uri.startsWith("/events/")) {
                            Long eventId = Long.parseLong(uri.substring("/events/".length()));
                            Integer hits = map.get("hits") instanceof Number n ? n.intValue() : 0; // ← "hits"
                            viewsMap.put(eventId, hits);
                        }
                    }
                }
            }
            eventIds.forEach(id -> viewsMap.putIfAbsent(id, 0));
            return viewsMap;
        } catch (Exception e) {
            log.warn("Не удалось получить views для событий {}: {}", eventIds, e.getMessage());
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0));
        }
    }

    private Set<EventShortDto> getEventsShorts(Set<Event> events) {
        if (events == null || events.isEmpty()) {
            return Set.of();
        }

        List<Event> eventList = new ArrayList<>(events);
        List<Long> eventIds = eventList.stream().map(Event::getId).toList();
        List<Long> ownerIds = eventList.stream().map(Event::getOwnerId).distinct().toList();

        // confirmedRequests
        Map<Long, Long> confirmedMap;
        try {
            confirmedMap = requestClient.getConfirmedCounts(eventIds);
        } catch (Exception e) {
            log.warn("Не удалось получить confirmedRequests для событий {}: {}", eventIds, e.getMessage());
            confirmedMap = eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }

        // initiator
        Map<Long, UserShortDto> initiatorMap;
        try {
            List<UserShortDto> users = userClient.getByIds(ownerIds);
            initiatorMap = users.stream().collect(Collectors.toMap(UserShortDto::getId, u -> u));
        } catch (Exception e) {
            log.warn("Не удалось получить пользователей {}: {}", ownerIds, e.getMessage());
            initiatorMap = ownerIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> new UserShortDto(id, "Unknown User")));
        }

        // views
        Map<Long, Integer> viewsMap = getEventsViewsMap(eventIds);

        Map<Long, UserShortDto> finalInitiatorMap = initiatorMap;
        Map<Long, Long> finalConfirmedMap = confirmedMap;
        return eventList.stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        EventCategoryMapper.toCategoryDtoFromCategory(event.getCategory()),
                        finalInitiatorMap.getOrDefault(event.getOwnerId(), new UserShortDto(event.getOwnerId(), "Unknown User")),
                        finalConfirmedMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0)
                ))
                .collect(Collectors.toSet());
    }
}