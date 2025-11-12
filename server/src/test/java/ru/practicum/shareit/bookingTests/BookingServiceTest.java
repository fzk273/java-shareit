package ru.practicum.shareit.bookingTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.request.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.DataConflictException;
import ru.practicum.shareit.exceptions.NotEnoughPrivilegesException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    BookingService service;

    private User booker;
    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(10L)
                .name("owner")
                .email("o@mail")
                .build();
        booker = User.builder()
                .id(20L)
                .name("booker")
                .email("b@mail")
                .build();
        item = Item.builder()
                .id(100L)
                .name("item")
                .description("desc")
                .available(true)
                .owner(owner)
                .build();
    }

    @Test
    void createBooking_success() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        when(userRepository.findById(20L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItem_IdAndStatusInAndStartLessThanAndEndGreaterThan(
                eq(100L), anyCollection(), eq(end), eq(start))
        ).thenReturn(false);

        Booking saved = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);

        BookingCreateRequestDto req = BookingCreateRequestDto.builder()
                .itemId(100L).start(start).end(end).build();

        BookingResponseDto dto = service.createBooking(20L, req);

        assertEquals(1L, dto.getId());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_throws_whenEndBeforeStart() {
        BookingCreateRequestDto req = BookingCreateRequestDto.builder()
                .itemId(100L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(BadRequestException.class, () -> service.createBooking(20L, req));
    }

    @Test
    void createBooking_throws_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        BookingCreateRequestDto req = BookingCreateRequestDto.builder()
                .itemId(100L).start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build();

        assertThrows(NotFoundException.class, () -> service.createBooking(1L, req));
    }

    @Test
    void createBooking_throws_whenItemNotFound() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(100L)).thenReturn(Optional.empty());

        BookingCreateRequestDto req = BookingCreateRequestDto.builder()
                .itemId(100L).start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build();

        assertThrows(NotFoundException.class, () -> service.createBooking(20L, req));
    }

    @Test
    void createBooking_throws_whenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(20L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        BookingCreateRequestDto req = BookingCreateRequestDto.builder()
                .itemId(100L).start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build();

        assertThrows(BadRequestException.class, () -> service.createBooking(20L, req));
    }

    @Test
    void createBooking_throws_whenOwnerBooksOwnItem() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        BookingCreateRequestDto req = BookingCreateRequestDto.builder()
                .itemId(100L).start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build();

        assertThrows(NotEnoughPrivilegesException.class, () -> service.createBooking(10L, req));
    }

    @Test
    void createBooking_throws_whenOverlaps() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItem_IdAndStatusInAndStartLessThanAndEndGreaterThan(eq(100L), anyCollection(), any(), any()))
                .thenReturn(true);

        BookingCreateRequestDto req = BookingCreateRequestDto.builder()
                .itemId(100L).start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2)).build();

        assertThrows(BadRequestException.class, () -> service.createBooking(20L, req));
    }


    @Test
    void approveBooking_success_approveTrue() {
        Booking waiting = Booking.builder()
                .id(5L).item(item).booker(booker).status(BookingStatus.WAITING).build();

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(waiting));
        when(userRepository.existsById(10L)).thenReturn(true); // owner exists
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDto dto = service.approveBooking(10L, 5L, true);
        assertEquals(BookingStatus.APPROVED, dto.getStatus());
    }

    @Test
    void approveBooking_success_rejectFalse() {
        Booking waiting = Booking.builder()
                .id(6L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingRepository.findById(6L)).thenReturn(Optional.of(waiting));
        when(userRepository.existsById(10L)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDto dto = service.approveBooking(10L, 6L, false);
        assertEquals(BookingStatus.REJECTED, dto.getStatus());
    }

    @Test
    void approveBooking_throws_whenNotWaiting() {
        Booking b = Booking.builder()
                .id(7L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(b));

        assertThrows(DataConflictException.class, () -> service.approveBooking(10L, 7L, true));
    }

    @Test
    void approveBooking_throws_whenUserMissing() {
        Booking waiting = Booking.builder()
                .id(8L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        when(bookingRepository.findById(8L)).thenReturn(Optional.of(waiting));
        when(userRepository.existsById(10L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.approveBooking(10L, 8L, true));
    }

    @Test
    void approveBooking_throws_whenNotOwner() {
        Booking waiting = Booking.builder()
                .id(9L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        when(bookingRepository.findById(9L)).thenReturn(Optional.of(waiting));
        when(userRepository.existsById(20L)).thenReturn(true);

        assertThrows(NotEnoughPrivilegesException.class, () -> service.approveBooking(20L, 9L, true));
    }

    @Test
    void getBookingById_success_forOwner() {
        Booking b = Booking.builder()
                .id(12L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findById(12L)).thenReturn(Optional.of(b));
        when(userRepository.existsById(10L)).thenReturn(true);

        BookingResponseDto dto = service.getBookingById(10L, 12L);
        assertEquals(12L, dto.getId());
    }

    @Test
    void getBookingById_throws_notFound() {
        when(bookingRepository.findById(1000L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getBookingById(10L, 1000L));
    }

    @Test
    void getBookingById_throws_userMissing() {
        Booking b = Booking.builder()
                .id(13L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findById(13L)).thenReturn(Optional.of(b));
        when(userRepository.existsById(10L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> service.getBookingById(10L, 13L));
    }

    @Test
    void getBookingById_throws_accessDenied() {
        User stranger = User.builder()
                .id(99L)
                .name("s")
                .email("s@x")
                .build();
        Booking b = Booking.builder()
                .id(14L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findById(14L)).thenReturn(Optional.of(b));
        when(userRepository.existsById(99L)).thenReturn(true);

        assertThrows(NotEnoughPrivilegesException.class, () -> service.getBookingById(99L, 14L));
    }

    @Test
    void getBookingsByUser_all_usesPageable() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(booker));
        Page<Booking> page = new PageImpl<>(List.of(
                Booking.builder()
                        .id(1L)
                        .item(item)
                        .booker(booker)
                        .status(BookingStatus.APPROVED)
                        .build()
        ));
        when(bookingRepository.getBookingsByBookerId(eq(20L), any(PageRequest.class))).thenReturn(page);

        List<BookingResponseDto> list = service.getBookingsByUser(20L, BookingState.ALL, 0, 10);

        assertEquals(1, list.size());
        verify(bookingRepository).getBookingsByBookerId(eq(20L), any(PageRequest.class));
    }

    @Test
    void getBookingsByUser_current_branchCalled() {
        when(userRepository.findById(20L)).thenReturn(Optional.of(booker));
        when(bookingRepository.getCurrentBookings(20L)).thenReturn(List.of(
                Booking.builder()
                        .id(2L)
                        .item(item)
                        .booker(booker)
                        .build()
        ));

        List<BookingResponseDto> list = service.getBookingsByUser(20L, BookingState.CURRENT, 0, 10);
        assertEquals(1, list.size());
        verify(bookingRepository).getCurrentBookings(20L);
    }


    @Test
    void getBookingsByOwner_allBranch() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(bookingRepository.getBookingsByOwner(10L)).thenReturn(List.of(
                Booking.builder()
                        .id(1L)
                        .item(item)
                        .booker(booker)
                        .build()
        ));

        List<BookingResponseDto> list = service.getBookingsByOwner(10L, BookingState.ALL);
        assertEquals(1, list.size());
        verify(bookingRepository).getBookingsByOwner(10L);
    }

    @Test
    void getBookingsByOwner_waitingBranch() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(bookingRepository.getBookingsByOwnerAndStatus(10L, BookingStatus.WAITING)).thenReturn(List.of());

        service.getBookingsByOwner(10L, BookingState.WAITING);
        verify(bookingRepository).getBookingsByOwnerAndStatus(10L, BookingStatus.WAITING);
    }
}
