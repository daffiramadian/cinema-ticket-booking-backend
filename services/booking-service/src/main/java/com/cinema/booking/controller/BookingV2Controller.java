package com.cinema.booking.controller;

import com.cinema.booking.dto.BookingActionInput;
import com.cinema.booking.dto.BookingOutput;
import com.cinema.booking.dto.BookingV2Input;
import com.cinema.booking.dto.SeatAvailabilityOutput;
import com.cinema.booking.service.BookingV2Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v2")
public class BookingV2Controller {
    private final BookingV2Service service;

    public BookingV2Controller(BookingV2Service service) {
        this.service = service;
    }

    @GetMapping("/showtimes/{showtimeId}/seats")
    public List<SeatAvailabilityOutput> getSeatsByShowtime(@PathVariable String showtimeId) {
        return service.getSeatsByShowtime(showtimeId);
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingOutput createBooking(@Valid @RequestBody BookingV2Input input) {
        return service.createBookingV2(input);
    }

    @GetMapping("/bookings/{id}")
    public BookingOutput getBookingById(@PathVariable String id) {
        return service.getBookingByIdV2(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking " + id + " not found"));
    }

    @PatchMapping("/bookings/{id}")
    public BookingOutput applyBookingAction(@PathVariable String id,
                                            @Valid @RequestBody BookingActionInput input) {
        return service.applyBookingActionV2(id, input);
    }
}
