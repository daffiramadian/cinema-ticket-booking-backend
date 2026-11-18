package com.cinema.booking.service;

import com.cinema.booking.client.CatalogClient;
import com.cinema.booking.dto.BookingActionInput;
import com.cinema.booking.dto.BookingOutput;
import com.cinema.booking.dto.BookingV2Input;
import com.cinema.booking.dto.SeatAvailabilityOutput;
import com.cinema.booking.entity.Booking;
import com.cinema.booking.entity.SeatAvailability;
import com.cinema.booking.repository.BookingRepository;
import com.cinema.booking.repository.SeatAvailabilityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingV2Service {
    private static final Logger log = LoggerFactory.getLogger(BookingV2Service.class);
    private static final List<String> ROWS = List.of("A", "B", "C", "D", "E");
    private static final int COLS = 10;

    private final BookingRepository bookingRepository;
    private final SeatAvailabilityRepository seatRepository;
    private final CatalogClient catalogClient;

    public BookingV2Service(
            BookingRepository bookingRepository,
            SeatAvailabilityRepository seatRepository,
            CatalogClient catalogClient
    ) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public List<SeatAvailabilityOutput> getSeatsByShowtime(String showtimeId) {
        catalogClient.ensureShowtimeExists(showtimeId);
        ensureSeatMapInitialized(showtimeId);

        OffsetDateTime now = OffsetDateTime.now();
        List<SeatAvailability> seats = seatRepository.findByShowtimeIdOrderBySeatNumberAsc(showtimeId);
        for (SeatAvailability seat : seats) {
            releaseIfExpired(seat, now);
        }

        return seats.stream()
                .map(s -> new SeatAvailabilityOutput(s.getSeatNumber(), s.getRowLabel(), s.getSeatCol(), s.getStatus()))
                .toList();
    }

    @Transactional
    public BookingOutput createBookingV2(BookingV2Input input) {
        catalogClient.ensureShowtimeExists(input.showtimeId());
        ensureSeatMapInitialized(input.showtimeId());

        SeatAvailability seat = seatRepository.findForUpdate(input.showtimeId(), input.seatNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid seat: " + input.seatNumber()));

        OffsetDateTime now = OffsetDateTime.now();
        releaseIfExpired(seat, now);

        boolean activeBookingExists = bookingRepository.existsActiveBooking(
                input.showtimeId(), input.seatNumber(), now);
        if (!"AVAILABLE".equals(seat.getStatus()) || activeBookingExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat " + input.seatNumber() + " is not available for showtime " + input.showtimeId()
            );
        }

        String id = generateId();
        OffsetDateTime expiresAt = now.plusMinutes(15);
        Booking booking = new Booking(id, input.showtimeId(), input.seatNumber(), "PENDING_PAYMENT", expiresAt);
        Booking saved = bookingRepository.save(booking);

        seat.setStatus("HELD");
        seat.setBookingId(saved.getId());
        seat.setHeldUntil(expiresAt);
        seatRepository.save(seat);
        log.info("Booking created: id={}, showtimeId={}, seatNumber={}, status={}",
                saved.getId(), saved.getShowtimeId(), saved.getSeatNumber(), saved.getStatus());

        return mapToOutput(saved);
    }

    public Optional<BookingOutput> getBookingByIdV2(String id) {
        return bookingRepository.findById(id).map(this::mapToOutput);
    }

    @Transactional
    public BookingOutput applyBookingActionV2(String id, BookingActionInput input) {
        String action = input != null ? input.getAction() : null;
        if (action == null || action.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action: must not be blank");
        }
        if (!"CANCEL".equals(action) && !"CONFIRM".equals(action)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported action: " + action);
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking " + id + " not found"));

        if ("CANCEL".equals(action)) {
            return cancelBooking(booking);
        }
        return confirmBooking(booking);
    }

    private BookingOutput cancelBooking(Booking booking) {
        requirePendingBooking(booking, "CANCELLED", "cancel");
        SeatAvailability seat = lockHeldSeatForBooking(booking);

        booking.setStatus("CANCELLED");
        Booking saved = bookingRepository.save(booking);

        seat.setStatus("AVAILABLE");
        seat.setBookingId(null);
        seat.setHeldUntil(null);
        seatRepository.save(seat);
        log.info("Booking cancelled: id={}, showtimeId={}, seatNumber={}",
                saved.getId(), saved.getShowtimeId(), saved.getSeatNumber());

        return mapToOutput(saved);
    }

    private BookingOutput confirmBooking(Booking booking) {
        requirePendingBooking(booking, "CONFIRMED", "confirm");
        SeatAvailability seat = lockHeldSeatForBooking(booking);

        OffsetDateTime now = OffsetDateTime.now();
        if ((booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(now))
                || (seat.getHeldUntil() != null && seat.getHeldUntil().isBefore(now))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking " + booking.getId() + " hold has expired");
        }

        booking.setStatus("CONFIRMED");
        Booking saved = bookingRepository.save(booking);

        seat.setStatus("BOOKED");
        seat.setHeldUntil(null);
        seatRepository.save(seat);
        log.info("Booking confirmed: id={}, showtimeId={}, seatNumber={}",
                saved.getId(), saved.getShowtimeId(), saved.getSeatNumber());

        return mapToOutput(saved);
    }

    private void requirePendingBooking(Booking booking, String targetStatus, String verb) {
        String id = booking.getId();
        String currentStatus = booking.getStatus();
        if (targetStatus.equals(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking " + id + " is already " + targetStatus);
        }
        if (!"PENDING_PAYMENT".equals(currentStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot " + verb + " booking " + id + " in status " + currentStatus
            );
        }
    }

    private SeatAvailability lockHeldSeatForBooking(Booking booking) {
        String id = booking.getId();
        SeatAvailability seat = seatRepository.findForUpdate(booking.getShowtimeId(), booking.getSeatNumber())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Seat " + booking.getSeatNumber() + " state is inconsistent for booking " + id
                ));

        if (!"HELD".equals(seat.getStatus()) || !id.equals(seat.getBookingId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Seat " + booking.getSeatNumber() + " is not held by booking " + id
            );
        }
        return seat;
    }

    private void ensureSeatMapInitialized(String showtimeId) {
        if (seatRepository.existsByShowtimeId(showtimeId)) {
            return;
        }
        List<SeatAvailability> newSeats = new ArrayList<>(ROWS.size() * COLS);
        for (String row : ROWS) {
            for (int col = 1; col <= COLS; col++) {
                String seatNo = String.format("%s%02d", row, col);
                newSeats.add(new SeatAvailability(showtimeId, seatNo, row, col, "AVAILABLE"));
            }
        }
        try {
            seatRepository.saveAll(newSeats);
        } catch (DataIntegrityViolationException ignored) {
            // Concurrent initialization already created the rows
        }
    }

    private void releaseIfExpired(SeatAvailability seat, OffsetDateTime now) {
        if ("HELD".equals(seat.getStatus()) && seat.getHeldUntil() != null && seat.getHeldUntil().isBefore(now)) {
            String expiredBookingId = seat.getBookingId();
            seat.setStatus("AVAILABLE");
            seat.setBookingId(null);
            seat.setHeldUntil(null);
            seatRepository.save(seat);
            log.info("Expired seat hold released: showtimeId={}, seatNumber={}, bookingId={}",
                    seat.getShowtimeId(), seat.getSeatNumber(), expiredBookingId);
        }
    }

    private String generateId() {
        int maxNum = bookingRepository.findAll().stream()
                .map(Booking::getId)
                .filter(id -> id != null && id.startsWith("BK-"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("BK-%03d", maxNum + 1);
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
