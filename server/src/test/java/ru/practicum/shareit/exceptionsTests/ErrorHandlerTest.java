package ru.practicum.shareit.exceptionsTests;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exceptions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void handleNotFound_returnsErrorResponse() {
        String msg = "not found";
        ErrorResponse resp = handler.handleNotFoundException(new NotFoundException(msg));
        assertEquals(msg, resp.getError());
    }

    @Test
    void handleConflict_returnsErrorResponse() {
        String msg = "conflict";
        ErrorResponse resp = handler.handleDataConflictException(new DataConflictException(msg));
        assertEquals(msg, resp.getError());
    }

    @Test
    void handleBadRequest_returnsErrorResponse() {
        String msg = "bad";
        ErrorResponse resp = handler.handleBadRequestException(new BadRequestException(msg));
        assertEquals(msg, resp.getError());
    }

    @Test
    void handleForbidden_returnsErrorResponse() {
        String msg = "forbidden";
        ErrorResponse resp = handler.handleNotEnoughPrivilegesException(new NotEnoughPrivilegesException(msg));
        assertEquals(msg, resp.getError());
    }
}
