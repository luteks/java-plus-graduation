package ru.yandex.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.request.model.RequestStatus;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}
