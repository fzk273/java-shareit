package ru.practicum.shareit.booking.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class BookingResponseDto {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime start;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime end;
    private ItemResponseDto item;
    private UserResponseDto booker;
    private BookingStatus status;

    public BookingResponseDto(Long id, LocalDateTime start, LocalDateTime end,
                              ItemResponseDto item, UserResponseDto booker, BookingStatus status) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.item = item;
        this.booker = booker;
        this.status = status;
    }

}
