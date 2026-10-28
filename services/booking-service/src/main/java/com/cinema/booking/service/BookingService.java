package com.cinema.booking.service;

import com.cinema.booking.dto.BookingInput;
import com.cinema.booking.dto.BookingOutput;
import com.cinema.booking.entity.Booking;
import com.cinema.booking.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public Optional<BookingOutput> getBookingById(String id) {
        return repository.findById(id).map(this::mapToOutput);
    }

    @Transactional
    public BookingOutput createBooking(BookingInput input) {
        // Check for active booking (PENDING_PAYMENT or CONFIRMED)
        if (repository.existsByShowtimeIdAndSeatNumberAndStatusIn(
                input.showtimeId(), input.seatNumber(), List.of("PENDING_PAYMENT", "CONFIRMED"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already booked for this showtime");
        }

        String id = generateId();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(15);
        Booking booking = new Booking(id, input.showtimeId(), input.seatNumber(), "PENDING_PAYMENT", expiresAt);
        Booking saved = repository.save(booking);
        return mapToOutput(saved);
    }

    private String generateId() {
        long count = repository.count();
        return String.format("BK-%03d", count + 1);
    }

    private BookingOutput mapToOutput(Booking booking) {
        return new BookingOutput(
                booking.getId(),
                booking.getShowtimeId(),
                booking.getSeatNumber(),
                booking.getStatus(),
                booking.getExpiresAt()
        );
    }
}
