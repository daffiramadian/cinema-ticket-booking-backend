package com.cinema.booking.controller;

import com.cinema.booking.dto.BookingInput;
import com.cinema.booking.dto.BookingOutput;
import com.cinema.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingOutput createBooking(@Valid @RequestBody BookingInput input) {
        return service.createBooking(input);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingOutput> getBookingById(@PathVariable String id) {
        Optional<BookingOutput> opt = service.getBookingById(id);
        return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
