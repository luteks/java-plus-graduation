package ru.yandex.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<ru.yandex.practicum.stats.model.Hit, Long> {

    @Query(nativeQuery = true,
            value = "SELECT a.name AS app, h.uri AS uri, COUNT(h.id) AS hits " +
                    "FROM hit h " +
                    "JOIN app a ON a.id = h.app_id " +
                    "WHERE h.date_time BETWEEN ?1 AND ?2 " +
                    "GROUP BY h.uri, a.name " +
                    "ORDER BY COUNT(h.id) DESC")
    List<Object[]> findAllHitsRaw(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query(nativeQuery = true,
            value = "SELECT DISTINCT a.name AS app, h.uri AS uri " +
                    "FROM hit h " +
                    "JOIN app a ON a.id = h.app_id " +
                    "WHERE h.date_time BETWEEN ?1 AND ?2")
    List<Object[]> findAllUniqueHitsRaw(LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query(nativeQuery = true,
            value = "SELECT a.name AS app, h.uri AS uri, COUNT(h.id) AS hits " +
                    "FROM hit h " +
                    "JOIN app a ON a.id = h.app_id " +
                    "WHERE h.date_time BETWEEN ?1 AND ?2 AND h.uri IN (?3) " +
                    "GROUP BY h.uri, a.name " +
                    "ORDER BY COUNT(h.id) DESC")
    List<Object[]> findHitsByUrisRaw(LocalDateTime startDateTime, LocalDateTime endDateTime, List<String> uris);

    @Query(nativeQuery = true,
            value = "SELECT DISTINCT a.name AS app, h.uri AS uri " +
                    "FROM hit h " +
                    "JOIN app a ON a.id = h.app_id " +
                    "WHERE h.date_time BETWEEN ?1 AND ?2 AND h.uri IN (?3)")
    List<Object[]> findUniqueHitsByUrisRaw(LocalDateTime startDateTime, LocalDateTime endDateTime, List<String> uris);
}