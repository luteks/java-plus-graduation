package ru.yandex.practicum.request.exception;

public class RepeatUserRequestorException extends RuntimeException {
    public RepeatUserRequestorException(String message) {
        super(message);
    }
}