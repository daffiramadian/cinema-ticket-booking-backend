package com.cinema.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record BookingInput(
    @NotBlank(message = "Showtime ID is required")
    String showtimeId,
    
    @NotBlank(message = "Seat number is required")
    String seatNumber
) {}
