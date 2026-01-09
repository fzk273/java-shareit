package ru.practicum.shareitgateway.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.booking.dto.request.BookingCreateRequestDto;
import ru.practicum.shareit.booking.enums.BookingState;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(BookingApiClient.class)
@TestPropertySource(properties = {
        "shareit.server.url=http://localhost:9090"
})
class BookingApiClientTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private BookingApiClient bookingApiClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_sendsPostWithUserHeaderAndBody() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(10L)
                .start(start)
                .end(end)
                .build();

        mockServer.expect(once(),
                        requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(USER_HEADER, "5"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":1,\"status\":\"WAITING\"}"));

        var response = bookingApiClient.createBooking(5L, dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void approveBooking_sendsPatchWithQueryParam() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/bookings/10?approved=true"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(USER_HEADER, "7"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":10,\"status\":\"APPROVED\"}"));

        var response = bookingApiClient.approveBooking(7L, 10L, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getBookingById_sendsGetWithUserHeader() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/bookings/42"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "3"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":42,\"status\":\"WAITING\"}"));

        var response = bookingApiClient.getBookingById(3L, 42L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getBookingsByUser_sendsGetWithoutQueryStringButNotCrashing() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "9"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[{\"id\":1},{\"id\":2}]"));

        var response = bookingApiClient.getBookingsByUser(9L, BookingState.WAITING, 0, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getBookingsByOwner_sendsGetOwnerWithoutQueryString() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/bookings/owner"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "11"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[{\"id\":5}]"));

        var response = bookingApiClient.getBookingsByOwner(11L, BookingState.ALL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }
}
