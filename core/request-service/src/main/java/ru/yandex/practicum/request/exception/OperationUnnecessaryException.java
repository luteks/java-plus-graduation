package ru.yandex.practicum.request.exception;

public class OperationUnnecessaryException extends RuntimeException {
    public OperationUnnecessaryException(String message) {
        super(message);
    }
}
