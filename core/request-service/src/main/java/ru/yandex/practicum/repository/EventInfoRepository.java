package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.EventInfo;

@Repository
public interface EventInfoRepository extends JpaRepository<EventInfo, Long> {
}