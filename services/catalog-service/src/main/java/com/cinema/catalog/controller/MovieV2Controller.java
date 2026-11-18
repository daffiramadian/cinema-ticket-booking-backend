package com.cinema.catalog.controller;

import com.cinema.catalog.dto.MovieV2Input;
import com.cinema.catalog.dto.MovieV2Output;
import com.cinema.catalog.dto.MovieV2PatchInput;
import com.cinema.catalog.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/{id}")
    public MovieV2Output replaceMovie(@PathVariable String id,
                                      @Valid @RequestBody MovieV2Input input) {
        return service.replaceMovieV2(id, input)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie " + id + " not found"));
    }

    @PatchMapping("/{id}")
    public MovieV2Output updateMovie(@PathVariable String id,
                                     @Valid @RequestBody MovieV2PatchInput input) {
        if (input == null || input.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one editable field must be provided");
        }
        return service.partialUpdateMovieV2(id, input)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie " + id + " not found"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable String id) {
        service.deleteMovieV2(id);
    }
}
