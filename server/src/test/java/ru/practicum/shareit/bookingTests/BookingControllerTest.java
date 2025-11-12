package ru.practicum.shareit.bookingTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.request.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_success() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingCreateRequestDto req = BookingCreateRequestDto.builder()
                .itemId(10L)
                .start(start)
                .end(end)
                .build();

        BookingResponseDto resp = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .build();

        given(bookingService.createBooking(eq(5L), any(BookingCreateRequestDto.class)))
                .willReturn(resp);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }


    @Test
    void approveBooking_success() throws Exception {
        BookingResponseDto resp = BookingResponseDto.builder()
                .id(7L)
                .status(BookingStatus.APPROVED)
                .build();

        given(bookingService.approveBooking(2L, 7L, true))
                .willReturn(resp);

        mockMvc.perform(patch("/bookings/7")
                        .header(USER_HEADER, 2L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_success() throws Exception {
        BookingResponseDto resp = BookingResponseDto.builder()
                .id(9L)
                .status(BookingStatus.WAITING)
                .build();

        given(bookingService.getBookingById(3L, 9L))
                .willReturn(resp);

        mockMvc.perform(get("/bookings/9")
                        .header(USER_HEADER, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void getBookingsByUser_success_withPagingAndState() throws Exception {
        BookingResponseDto b = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();
        given(bookingService.getBookingsByUser(4L, BookingState.WAITING, 0, 5)).willReturn(List.of(b));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 4L)
                        .param("page", "0")
                        .param("size", "5")
                        .param("bookingState", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getBookingsByOwner_success_defaultStateAll() throws Exception {
        BookingResponseDto b = BookingResponseDto.builder().id(2L).status(BookingStatus.APPROVED).build();
        given(bookingService.getBookingsByOwner(8L, BookingState.ALL)).willReturn(List.of(b));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
    }
}
