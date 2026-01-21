package ru.yandex.practicum.request.dto;


import lombok.Getter;
import ru.yandex.practicum.dto.RequestStatus;

import java.util.List;

@Getter
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}