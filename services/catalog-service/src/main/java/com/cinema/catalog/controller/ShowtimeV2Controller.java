package com.cinema.catalog.controller;

import com.cinema.catalog.dto.ShowtimeInput;
import com.cinema.catalog.dto.ShowtimeOutput;
import com.cinema.catalog.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
public class ShowtimeV2Controller {
    private final ShowtimeService service;

    public ShowtimeV2Controller(ShowtimeService service) {
        this.service = service;
    }

    @GetMapping("/movies/{movieId}/showtimes")
    public List<ShowtimeOutput> getShowtimesByMovie(@PathVariable String movieId) {
        return service.getShowtimesByMovieId(movieId);
    }

    @PostMapping("/showtimes")
    @ResponseStatus(HttpStatus.CREATED)
    public ShowtimeOutput createShowtime(@Valid @RequestBody ShowtimeInput input) {
        return service.createShowtime(input);
    }

    @GetMapping("/showtimes/{id}")
    public ShowtimeOutput getShowtimeById(@PathVariable String id) {
        return service.getShowtimeById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime " + id + " not found"));
    }
}
