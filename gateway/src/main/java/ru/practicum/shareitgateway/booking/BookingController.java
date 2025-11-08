package ru.practicum.shareitgateway.booking;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.request.BookingCreateRequestDto;
import ru.practicum.shareit.booking.enums.BookingState;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingApiClient bookingApiClient;

    public BookingController(BookingApiClient bookingApiClient) {
        this.bookingApiClient = bookingApiClient;
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid BookingCreateRequestDto bookingCreateRequestDto) {
        return bookingApiClient.createBooking(userId, bookingCreateRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        return bookingApiClient.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long bookingId) {
        return bookingApiClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "ALL") BookingState bookingState,
            @RequestParam(defaultValue = "10") int size) {
        return bookingApiClient.getBookingsByUser(userId, bookingState, page, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(required = false, defaultValue = "ALL") BookingState state) {
        return bookingApiClient.getBookingsByOwner(ownerId, state);
    }
}
