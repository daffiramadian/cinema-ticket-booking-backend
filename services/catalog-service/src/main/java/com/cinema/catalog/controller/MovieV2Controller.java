package com.cinema.catalog.controller;

import com.cinema.catalog.dto.MovieV2Input;
import com.cinema.catalog.dto.MovieV2Output;
import com.cinema.catalog.service.MovieService;
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
@RequestMapping("/api/v2/movies")
public class MovieV2Controller {
    private final MovieService service;

    public MovieV2Controller(MovieService service) {
        this.service = service;
    }

    @GetMapping
    public List<MovieV2Output> getAllMovies() {
        return service.getAllMoviesV2();
    }

    @GetMapping("/{id}")
    public MovieV2Output getMovieById(@PathVariable String id) {
        return service.getMovieByIdV2(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie " + id + " not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieV2Output createMovie(@Valid @RequestBody MovieV2Input input) {
        return service.createMovieV2(input);
    }
}
