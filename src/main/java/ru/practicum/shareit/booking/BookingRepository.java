package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.enums.BookingStatus;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId " +
            "ORDER BY b.start DESC")
    Page<Booking> getBookingsByBookerId(@Param("bookerId") Long bookerId, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND CURRENT_TIMESTAMP BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> getCurrentBookings(@Param("bookerId") Long bookerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> getPastBookings(@Param("bookerId") Long bookerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.start > CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> getFutureBookings(@Param("bookerId") Long bookerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE b.booker.id = :bookerId AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> getBookingsByStatus(@Param("bookerId") Long bookerId, @Param("status") BookingStatus status);

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> getBookingsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND CURRENT_TIMESTAMP BETWEEN b.start AND b.end " +
            "ORDER BY b.start DESC")
    List<Booking> getCurrentBookingsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> getPastBookingsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.start > CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> getFutureBookingsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> getBookingsByOwnerAndStatus(@Param("ownerId") Long ownerId,
                                              @Param("status") BookingStatus status);
}
