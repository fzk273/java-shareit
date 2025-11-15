package ru.practicum.shareit.item.dto.response;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.item.comments.dto.response.CommentResponseDto;

import java.util.List;

@Data
@Builder
public class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long lastBooking;
    private Long nextBooking;
    private List<BookingResponseDto> bookings;
    private List<CommentResponseDto> comments;
}
