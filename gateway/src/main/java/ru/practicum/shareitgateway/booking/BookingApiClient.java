package ru.practicum.shareitgateway.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.request.BookingCreateRequestDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareitgateway.client.ApiClient;

import java.util.Map;

@Service
public class BookingApiClient extends ApiClient {
    private static final String PREFIX = "/bookings";


    public BookingApiClient(RestTemplateBuilder restTemplate, @Value("${shareit.server.url}") String shareItServerUrl) {
        super(restTemplate
                .uriTemplateHandler(new DefaultUriBuilderFactory(shareItServerUrl + PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> createBooking(Long userId, BookingCreateRequestDto bookingCreateRequestDto) {
        return post("", userId, bookingCreateRequestDto);
    }

    public ResponseEntity<Object> approveBooking(Long ownerId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", ownerId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingsByUser(Long userId, BookingState bookingState, int page, int size) {
        Map<String, Object> parameters = Map.of("BookingState", bookingState,
                "page", page,
                "size", size);
        return get("", userId, parameters);
    }

    public ResponseEntity<Object> getBookingsByOwner(Long ownerId, BookingState bookingState) {
        Map<String, Object> parameters = Map.of("BookingState", bookingState);
        return get("/owner", ownerId, parameters);
    }
}
