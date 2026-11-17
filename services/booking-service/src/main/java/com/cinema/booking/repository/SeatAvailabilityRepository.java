package com.cinema.booking.repository;

import com.cinema.booking.entity.SeatAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {
    List<SeatAvailability> findByShowtimeIdOrderBySeatNumberAsc(String showtimeId);

    boolean existsByShowtimeId(String showtimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatAvailability s WHERE s.showtimeId = :showtimeId AND s.seatNumber = :seatNumber")
    Optional<SeatAvailability> findForUpdate(@Param("showtimeId") String showtimeId, @Param("seatNumber") String seatNumber);
}
