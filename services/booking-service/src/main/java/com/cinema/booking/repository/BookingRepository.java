package com.cinema.booking.repository;

import com.cinema.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    boolean existsByShowtimeIdAndSeatNumberAndStatusIn(String showtimeId, String seatNumber, List<String> statuses);
}
