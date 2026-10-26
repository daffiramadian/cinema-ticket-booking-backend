package com.cinema.catalog.controller;

import com.cinema.catalog.dto.MovieInput;
import com.cinema.catalog.dto.MovieOutput;
import com.cinema.catalog.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping
    public List<MovieOutput> getAllMovies() {
        return service.getAllMovies();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieOutput> getMovieById(@PathVariable String id) {
        return service.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieOutput createMovie(@Valid @RequestBody MovieInput input) {
        return service.createMovie(input);
    }
}
