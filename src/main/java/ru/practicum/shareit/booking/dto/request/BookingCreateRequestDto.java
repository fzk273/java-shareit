package ru.practicum.shareit.booking.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class BookingCreateRequestDto {
    @NotNull
    @Future
    private LocalDateTime start;
    @NotNull
    @Future
    private LocalDateTime end;
    @NotNull
    private Long itemId;

    public BookingCreateRequestDto(LocalDateTime start, LocalDateTime end, Long itemId) {
        this.start = start;
        this.end = end;
        this.itemId = itemId;
    }

    public BookingCreateRequestDto() {
    }
}
