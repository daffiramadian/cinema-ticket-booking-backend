package com.cinema.catalog.dto;

import java.time.LocalDate;
import java.util.List;

public record MovieV2Output(
        String id,
        String title,
        String synopsis,
        Integer durationMinutes,
        List<String> genres,
        String posterUrl,
        String ageRating,
        String director,
        List<String> cast,
        String trailerUrl,
        LocalDate releaseDate
) {}
