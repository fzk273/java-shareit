package ru.practicum.shareit.exceptions;

public class NotEnoughPrivilegesException extends RuntimeException {
    public NotEnoughPrivilegesException(String message) {
        super(message);
    }
}
