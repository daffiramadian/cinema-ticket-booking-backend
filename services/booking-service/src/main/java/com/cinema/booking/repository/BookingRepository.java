package com.cinema.booking.repository;

import com.cinema.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    boolean existsByShowtimeIdAndSeatNumberAndStatusIn(String showtimeId, String seatNumber, List<String> statuses);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.showtimeId = :showtimeId
              AND b.seatNumber = :seatNumber
              AND (b.status = 'CONFIRMED'
                   OR (b.status = 'PENDING_PAYMENT' AND (b.expiresAt IS NULL OR b.expiresAt >= :now)))
            """)
    boolean existsActiveBooking(
            @Param("showtimeId") String showtimeId,
            @Param("seatNumber") String seatNumber,
            @Param("now") OffsetDateTime now
    );
}
