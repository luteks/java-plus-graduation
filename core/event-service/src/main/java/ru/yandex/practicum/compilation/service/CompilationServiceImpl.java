package ru.yandex.practicum.compilation.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.compilation.dto.CompilationDto;
import ru.yandex.practicum.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.compilation.mapper.CompilationMapper;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.compilation.repository.CompilationRepository;
import ru.yandex.practicum.dto.CategoryDto;
import ru.yandex.practicum.dto.EventShortDto;
import ru.yandex.practicum.dto.UserShortDto;
import ru.yandex.practicum.event.mapper.EventMapper;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.EntityNotFoundException;
import ru.yandex.practicum.feign.CategoryFeign;
import ru.yandex.practicum.feign.UserFeign;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationMapper compilationMapper;
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final UserFeign userFeign;
    private final CategoryFeign categoryFeign;
    private final EventMapper eventMapper;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation newCompilation = new Compilation();
        newCompilation.setTitle(newCompilationDto.getTitle());
        newCompilation.setPinned(newCompilationDto.getPinned());

        if (newCompilationDto.getEvents() == null || newCompilationDto.getEvents().isEmpty()) {
            return compilationMapper.toCompilationDto(compilationRepository.save(newCompilation), new ArrayList<>());
        }
        List<Event> events = eventRepository.findAllByIdIsIn(newCompilationDto.getEvents());
        if (events.isEmpty()) {
            throw new EntityNotFoundException(Event.class, "Указанные события не найдены");
        }
        newCompilation.setEvents(events);
        newCompilation = compilationRepository.save(newCompilation);
        log.info("Сохраняем подборку в БД: {}", newCompilation);
        List<EventShortDto> eventDtos = getEventShortDtos(events);
        CompilationDto result = compilationMapper.toCompilationDto(newCompilation, eventDtos);
        log.info("Результат маппинга: {}", result);
        return result;
    }

    @Override
    public void delete(Long id) {
        compilationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Compilation.class, "(Подборка) c ID = " + id + ", не найдена"));

        compilationRepository.deleteById(id);
    }

    @Override
    public CompilationDto update(Long id, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Compilation.class, "(Подборка) c ID = " + id + ", не найдена"));
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllByIdIsIn(updateCompilationRequest.getEvents());
            if (events.isEmpty()) {
                throw new EntityNotFoundException(Event.class, "Указанные события не найдены");
            }
            compilation.setEvents(events);
        }
        compilation = compilationRepository.save(compilation);
        log.info("Обновляем подборку в БД: {}", compilation);
        if (compilation.getEvents() == null || compilation.getEvents().isEmpty()) {
            return compilationMapper.toCompilationDto(compilation, new ArrayList<>());
        }
        List<EventShortDto> eventDtos = getEventShortDtos(compilation.getEvents());
        CompilationDto result = compilationMapper.toCompilationDto(compilation, eventDtos);
        log.info("Результат маппинга: {}", result);
        return result;
    }

    @Override
    public Collection<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from, size);
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);
        log.info("Результат поиска подборок в БД, {}", compilations);
        if (compilations.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> eventsId = new HashSet<>();
        for (Compilation compilation : compilations) {
            Set<Long> compilationEvents = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toSet());
            eventsId.addAll(compilationEvents);
        }
        if (eventsId.isEmpty()) {
            return compilationMapper.toCompilationDtos(compilations);
        }
        List<Event> events = eventRepository.findAllByIdIsIn(eventsId);
        if (events.isEmpty()) {
            throw new EntityNotFoundException(Event.class, "Указанные события не найдены");
        }

        Map<Long, CompilationDto> compilationDtoMap = compilationMapper.toCompilationDtos(compilations)
                .stream().collect(Collectors.toMap(CompilationDto::getId, Function.identity()));
        List<EventShortDto> eventDtoList = getEventShortDtos(events);
        Map<Long, EventShortDto> eventDtoMap = eventDtoList.stream().collect(Collectors.toMap(EventShortDto::getId, Function.identity()));

        for (Compilation compilation : compilations) {
            CompilationDto dto = compilationDtoMap.get(compilation.getId());
            for (Event event : compilation.getEvents()) {
                dto.getEvents().add(eventDtoMap.get(event.getId()));
            }
        }
        log.info("Результат поиска подборок с заполненными полями: {}", compilationDtoMap);
        return compilationDtoMap.values();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(Compilation.class, "Подборка событий не найдена"));

        if (compilation.getEvents().isEmpty()) {
            return compilationMapper.toCompilationDto(compilation, new ArrayList<>());
        }

        Set<Long> eventsId = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toSet());
        List<Event> events = eventRepository.findAllByIdIsIn(eventsId);
        if (events.isEmpty()) {
            throw new EntityNotFoundException(Event.class, "Указанные события не найдены");
        }
        List<EventShortDto> eventDtos = getEventShortDtos(events);
        return compilationMapper.toCompilationDto(compilation, eventDtos);
    }

    private List<EventShortDto> getEventShortDtos(List<Event> events) {
        List<EventShortDto> dtos = eventMapper.toEventShortDto(events);
        log.info("Результат маппинга в EventShortDto: {}", dtos);
        addCategoriesDto(dtos, events);
        addUserShortDto(dtos, events);
        return dtos;
    }

    private List<EventShortDto> addCategoriesDto(List<EventShortDto> dtos, List<Event> events) {
        Map<Long, EventShortDto> dtoMap = dtos.stream().collect(Collectors.toMap(EventShortDto::getId, Function.identity()));

        Set<Long> categoriesId = events.stream().map(Event::getCategoryId).collect(Collectors.toSet());
        Map<Long, CategoryDto> categories;
        try {
            categories = categoryFeign.getCategoryById(categoriesId);
            log.info("Получаем категории из category-service: {}", categories);
        } catch (FeignException e) {
            throw new EntityNotFoundException(CategoryDto.class, e.getMessage());
        }
        for (Event event : events) {
            dtoMap.get(event.getId()).setCategory(categories.get(event.getCategoryId()));
        }
        log.info("Добавляем категории: {}", dtos);
        return dtos;
    }

    private List<EventShortDto> addUserShortDto(List<EventShortDto> dtos, List<Event> events) {
        Map<Long, EventShortDto> dtoMap = dtos.stream().collect(Collectors.toMap(EventShortDto::getId, Function.identity()));

        Set<Long> usersId = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> users;
        try {
            users = userFeign.findUserShortDtoById(usersId);
            log.info("Получаем пользователей из user-service: {}", users);
        } catch (FeignException e) {
            throw new EntityNotFoundException(UserShortDto.class, e.getMessage());
        }
        for (Event event : events) {
            dtoMap.get(event.getId()).setInitiator(users.get(event.getInitiatorId()));
        }
        log.info("Добавляем пользователей: {}", dtos);
        return dtos;
    }
}