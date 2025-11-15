package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.request.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static BookingResponseDto toDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .booker(UserMapper.toDto(booking.getBooker()))
                .end(booking.getEnd())
                .start(booking.getStart())
                .status(booking.getStatus())
                .item(ItemMapper.itemToResponseDto(booking.getItem()))
                .build();
    }


    public static Booking bookingCreateResponseToEntity(BookingCreateRequestDto bookingCreateDto,
                                                        Item item, User booker) {
        return Booking.builder()
                .booker(booker)
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
    }
}
