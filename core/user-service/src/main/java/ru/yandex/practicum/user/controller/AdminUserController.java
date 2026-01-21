package ru.yandex.practicum.user.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.UserDto;
import ru.yandex.practicum.dto.UserShortDto;
import ru.yandex.practicum.user.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll(@RequestParam(required = false) List<Long> ids,
                                @RequestParam(defaultValue = "0") int from,
                                @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение всех пользователей: {}", ids);
        return userService.getAll(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Запрос на создание пользователя: {}", userDto);
        return userService.create(userDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("userId") long id) {
        log.info("Запрос на удаление пользователя: {}", id);
        userService.delete(id);
    }

    @GetMapping("/short/{userId}")
    public UserShortDto findUserShortDtoById(@PathVariable("userId") Long userId) {
        log.info("Запрос на поиск пользователя: {}", userId);
        return userService.findUserShortDtoById(userId);
    }

    @PostMapping("/short/map")
    public Map<Long, UserShortDto> findUserShortDtoById(@RequestBody Set<Long> usersId) {
        log.info("Запрос на получение пользователей по id: {}", usersId);
        return userService.findUserShortDtoById(usersId);
    }
}
