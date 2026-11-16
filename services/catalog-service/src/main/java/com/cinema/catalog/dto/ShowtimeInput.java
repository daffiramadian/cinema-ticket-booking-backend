package com.cinema.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ShowtimeInput(
        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^MV-[0-9]{3,}$", message = "must match MV-NNN")
        String movieId,

        @NotBlank(message = "must not be blank")
        @Size(max = 200, message = "must be at most 200 characters")
        String cinemaName,

        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must be at most 100 characters")
        String studioName,

        @NotNull(message = "must not be null")
        OffsetDateTime startsAt,

        @NotNull(message = "must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "must be greater than 0")
        BigDecimal price
) {}
