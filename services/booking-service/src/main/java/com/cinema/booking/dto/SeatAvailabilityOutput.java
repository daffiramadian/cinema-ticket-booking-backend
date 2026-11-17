package com.cinema.booking.dto;

public record SeatAvailabilityOutput(
        String seatNumber,
        String row,
        Integer number,
        String status
) {}
