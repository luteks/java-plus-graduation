package ru.yandex.practicum.user.service;


import ru.yandex.practicum.dto.UserDto;
import ru.yandex.practicum.dto.UserShortDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(UserDto userDto);

    void delete(Long id);

    UserShortDto findUserShortDtoById(Long userId);

    Map<Long, UserShortDto> findUserShortDtoById(Set<Long> usersId);

}
