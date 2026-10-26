package com.cinema.booking.dto;

import java.time.OffsetDateTime;

public record BookingOutput(
    String id,
    String showtimeId,
    String seatNumber,
    String status,
    OffsetDateTime expiresAt
) {}
