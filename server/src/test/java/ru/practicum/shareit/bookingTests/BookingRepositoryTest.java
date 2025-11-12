package ru.practicum.shareit.bookingTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setup() {
        owner = userRepository.save(User.builder()
                .name("owner")
                .email("o@mail")
                .build());
        booker = userRepository.save(User.builder()
                .name("booker")
                .email("b@mail")
                .build());

        item = itemRepository.save(Item.builder()
                .name("drill")
                .description("desc")
                .available(true)
                .owner(owner)
                .build());

        // PAST
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .build());
        // CURRENT
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.APPROVED)
                .build());
        // FUTURE (WAITING)
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.WAITING)
                .build());
        // FUTURE (REJECTED)
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(5))
                .status(BookingStatus.REJECTED)
                .build());
    }

    @Test
    void getBookingsByBookerId_paged() {
        Page<Booking> page = bookingRepository.getBookingsByBookerId(booker.getId(), PageRequest.of(0, 10));
        assertTrue(page.getTotalElements() >= 3);
        assertTrue(page.getContent().stream()
                .allMatch(b -> b.getBooker().getId().equals(booker.getId())));
    }

    @Test
    void current_past_future_status_queries() {
        List<Booking> current = bookingRepository.getCurrentBookings(booker.getId());
        List<Booking> past = bookingRepository.getPastBookings(booker.getId());
        List<Booking> future = bookingRepository.getFutureBookings(booker.getId());
        List<Booking> waiting = bookingRepository.getBookingsByStatus(booker.getId(), BookingStatus.WAITING);
        List<Booking> rejected = bookingRepository.getBookingsByStatus(booker.getId(), BookingStatus.REJECTED);

        assertEquals(1, current.size());
        assertEquals(1, past.size());
        assertTrue(future.size() >= 2);
        assertEquals(1, waiting.size());
        assertEquals(1, rejected.size());
    }

    @Test
    void owner_queries() {
        List<Booking> all = bookingRepository.getBookingsByOwner(owner.getId());
        List<Booking> cur = bookingRepository.getCurrentBookingsByOwner(owner.getId());
        List<Booking> past = bookingRepository.getPastBookingsByOwner(owner.getId());
        List<Booking> fut = bookingRepository.getFutureBookingsByOwner(owner.getId());
        List<Booking> waiting = bookingRepository.getBookingsByOwnerAndStatus(owner.getId(), BookingStatus.WAITING);

        assertEquals(4, all.size());
        assertEquals(1, cur.size());
        assertEquals(1, past.size());
        assertTrue(fut.size() >= 2);
        assertEquals(1, waiting.size());
    }


}
