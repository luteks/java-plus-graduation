package ru.yandex.practicum.user.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.UserDto;
import ru.yandex.practicum.dto.UserShortDto;
import ru.yandex.practicum.user.exception.UserNotFoundException;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.model.UserMapper;
import ru.yandex.practicum.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);

        if (ids != null && !ids.isEmpty()) {
            return userMapper.toUserDtoList(userRepository.findByIdIn(ids, pageable));
        } else {
            return userMapper.toUserDtoList(userRepository.findAll(pageable).getContent());
        }
    }

    public UserDto create(UserDto userDto) {
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь c ID - " + id + ", не найден."));
        userRepository.deleteById(id);
    }

    public UserShortDto findUserShortDtoById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь c ID - " + userId + ", не найден."));
        log.info("Найден пользователь: {}", user);
        UserShortDto dto = userMapper.toUserShortDto(user);
        log.info("Результат маппинга: {}", dto);
        return dto;
    }

    public Map<Long, UserShortDto> findUserShortDtoById(Set<Long> usersId) {
        List<User> users = userRepository.findAllById(usersId);
        if (users.isEmpty()) {
            throw new UserNotFoundException("Пользователи не найдены.");
        }

        Map<Long, UserShortDto> result = userMapper.toUserShortDto(users).stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));

        if (usersId.size() > result.size()) {
            List<Long> notAvailabilityCategory = new ArrayList<>();
            for (Long id : usersId) {
                if (!result.containsKey(id)) {
                    notAvailabilityCategory.add(id);
                }
            }
            throw new UserNotFoundException("Не найдены пользователи с id: " + notAvailabilityCategory);
        }
        log.info("Результат поиска пользователей по id: {}", result);
        return result;
    }
}