package com.cinema.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BookingV2Input(
        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^ST-[0-9]{3,}$", message = "must match ST-NNN")
        String showtimeId,

        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^[A-E](0[1-9]|10)$", message = "must be between A01 and E10")
        String seatNumber
) {}
