package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
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

import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingService(BookingRepository bookingRepository,
                          UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingCreateRequestDto bookingCreateDto) {

        if (!bookingCreateDto.getEnd().isAfter(bookingCreateDto.getStart())) {
            throw new BadRequestException("Start time have to be before end time");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("There is no user with id: " + userId));
        Item item = itemRepository.findById(bookingCreateDto.getItemId())
                .orElseThrow(() -> new NotFoundException("There is no item with id: " + bookingCreateDto.getItemId()));
        if (!item.getAvailable()) {
            throw new BadRequestException("Item " + item.getId() + " is not available");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotEnoughPrivilegesException("User " + userId + " is owner of item " + item.getId());
        }
        List<BookingStatus> blockingStatuses = List.of(BookingStatus.APPROVED, BookingStatus.WAITING);
        boolean overlaps = bookingRepository.existsByItem_IdAndStatusInAndStartLessThanAndEndGreaterThan(
                item.getId(), blockingStatuses, bookingCreateDto.getEnd(), bookingCreateDto.getStart()
        );
        if (overlaps) {
            throw new BadRequestException("Your booking is overlapping for item: " + item.getId());
        }
        Booking booking = BookingMapper.bookingCreateResponseToEntity(bookingCreateDto, item, booker);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toDto(saved);

    }

    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("There is no booking with id: " + bookingId));
        if (BookingStatus.WAITING != booking.getStatus()) {
            throw new DataConflictException("Booking with id: " + bookingId + " is not in waiting state");
        }
        if (!userRepository.existsById(userId)) {
            throw new BadRequestException("there is no such user with id: " + userId);
        }

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotEnoughPrivilegesException("User " + userId + " is not the owner of item " + booking.getItem().getId());
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("There is no booking with id: " + bookingId));

        if (!userRepository.existsById(userId)) {
            throw new BadRequestException("there is no such user with id: " + userId);
        }

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotEnoughPrivilegesException("Access denied to see booking details");
        }
        return BookingMapper.toDto(booking);
    }

    @Transactional
    public List<BookingResponseDto> getBookingsByUser(Long userId, BookingState state, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("There is no user with id: " + userId));
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository.getCurrentBookings(userId);
            case PAST -> bookingRepository.getPastBookings(userId);
            case FUTURE -> bookingRepository.getFutureBookings(userId);
            case WAITING -> bookingRepository.getBookingsByStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.getBookingsByStatus(userId, BookingStatus.REJECTED);
            default -> bookingRepository.getBookingsByBookerId(userId, pageRequest).getContent();

        };
        return bookings.stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    @Transactional
    public List<BookingResponseDto> getBookingsByOwner(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("There is no user with id: " + userId));
        List<Booking> bookings = switch (state) {
            case WAITING -> bookingRepository.getBookingsByOwnerAndStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.getBookingsByOwnerAndStatus(userId, BookingStatus.REJECTED);
            case CURRENT -> bookingRepository.getCurrentBookingsByOwner(userId);
            case PAST -> bookingRepository.getPastBookingsByOwner(userId);
            case FUTURE -> bookingRepository.getFutureBookingsByOwner(userId);
            default -> bookingRepository.getBookingsByOwner(userId);
        };
        return bookings.stream()
                .map(BookingMapper::toDto)
                .toList();
    }

}
