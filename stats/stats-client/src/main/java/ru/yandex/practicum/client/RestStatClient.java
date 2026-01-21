package ru.yandex.practicum.client;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.ParamDto;
import ru.yandex.practicum.ParamHitDto;
import ru.yandex.practicum.ViewStats;
import ru.yandex.practicum.exception.InvalidRequestException;
import ru.yandex.practicum.exception.StatsServerUnavailable;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RestStatClient implements StatClient {
    private final DiscoveryClient discoveryClient;
    private RestClient restClient;
    @Value("${stat.client.url}")
    private String statUrl;

    @Override
    public void hit(ParamHitDto paramHitDto) {
        getRestClient().post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(paramHitDto)
                .retrieve()
                .onStatus(status -> status != HttpStatus.CREATED, (request, response) -> {
                    throw new InvalidRequestException(response.getStatusCode().value() + ": " + response.getBody());
                });
    }

    @Override
    public List<ViewStats> getStat(ParamDto paramDto) {
        return getRestClient().get().uri(uriBuilder -> uriBuilder.path("/stats")
                        .queryParam("start", paramDto.getStart().toString())
                        .queryParam("end", paramDto.getEnd().toString())
                        .queryParam("uris", paramDto.getUris())
                        .queryParam("unique", paramDto.getUnique())
                        .build())
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, (request, response) -> {
                    throw new InvalidRequestException(response.getStatusCode().value() + ": " + response.getBody());
                })
                .body(ParameterizedTypeReference.forType(List.class));
    }

    private ServiceInstance getInstance() {
        try {
            ServiceInstance serviceInstance = discoveryClient
                    .getInstances(statUrl)
                    .getFirst();
            log.info("Получаем {} uri: {}", statUrl, serviceInstance.getUri().toString());
            return serviceInstance;
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statUrl
            );
        }
    }

    private RestClient getRestClient() {
        ServiceInstance instance = getInstance();
        if (restClient == null) {
            this.restClient = RestClient.create(instance.getUri().toString());
        }
        return this.restClient;
    }
}