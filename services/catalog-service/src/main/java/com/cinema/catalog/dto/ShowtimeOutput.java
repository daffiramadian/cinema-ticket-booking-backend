package com.cinema.catalog.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ShowtimeOutput(
        String id,
        String movieId,
        String cinemaName,
        String studioName,
        OffsetDateTime startsAt,
        BigDecimal price
) {}
