package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.request.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;

import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;


    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody BookingCreateRequestDto bookingCreateRequestDto) {

        BookingResponseDto booking = bookingService.createBooking(userId, bookingCreateRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(booking);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> approveBooking(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {

        BookingResponseDto booking = bookingService.approveBooking(ownerId, bookingId, approved);
        return ResponseEntity.status(HttpStatus.OK)
                .body(booking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {

        BookingResponseDto bookingDto = bookingService.getBookingById(userId, bookingId);
        return new ResponseEntity<>(bookingDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "ALL") BookingState bookingState,
            @RequestParam(defaultValue = "10") int size) {
        List<BookingResponseDto> bookings = bookingService.getBookingsByUser(userId, bookingState, page, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(required = false, defaultValue = "ALL") BookingState state) {

        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(ownerId, state);
        return ResponseEntity.status(HttpStatus.OK)
                .body(bookings);
    }
}
