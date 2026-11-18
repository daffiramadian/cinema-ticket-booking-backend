package com.cinema.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record MovieV2PatchInput(
        @Pattern(regexp = "(?s)^.*\\S.*$", message = "must not be blank")
        @Size(max = 200, message = "must be at most 200 characters")
        String title,

        @Pattern(regexp = "(?s)^.*\\S.*$", message = "must not be blank")
        @Size(max = 2000, message = "must be at most 2000 characters")
        String synopsis,

        @Min(value = 1, message = "must be greater than or equal to 1")
        Integer durationMinutes,

        @Size(min = 1, message = "must not be empty")
        List<@NotBlank(message = "genre item must not be blank") String> genres,

        @Pattern(regexp = "^https://\\S+$", message = "must be a valid https:// URL")
        String posterUrl,

        @Pattern(regexp = "(?s)^.*\\S.*$", message = "must not be blank")
        @Size(max = 10, message = "must be at most 10 characters")
        String ageRating,

        @Pattern(regexp = "(?s)^.*\\S.*$", message = "must not be blank")
        @Size(max = 200, message = "must be at most 200 characters")
        String director,

        List<@NotBlank(message = "cast item must not be blank") String> cast,

        @Pattern(regexp = "^https://\\S+$", message = "must be a valid https:// URL")
        String trailerUrl,

        LocalDate releaseDate
) {
    public boolean isEmpty() {
        return title == null
                && synopsis == null
                && durationMinutes == null
                && genres == null
                && posterUrl == null
                && ageRating == null
                && director == null
                && cast == null
                && trailerUrl == null
                && releaseDate == null;
    }
}
