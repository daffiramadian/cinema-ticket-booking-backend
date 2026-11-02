package com.cinema.catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
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

import com.cinema.catalog.dto.MovieInput;
import com.cinema.catalog.dto.MovieOutput;
import com.cinema.catalog.dto.MoviePatchInput;
import com.cinema.catalog.service.MovieService;

import jakarta.validation.Valid;

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

    @PutMapping("/{id}")
    public ResponseEntity<MovieOutput> replaceMovie(@PathVariable String id,
                                                     @Valid @RequestBody MovieInput input) {
        return service.updateMovie(id, input)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable String id,
                                         @RequestBody MoviePatchInput input) {
        if (input.getTitle() == null && input.getDurationMinutes() == null && input.getDescription() == null) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                    "At least one field must be provided");
            problem.setTitle("Bad Request");
            return ResponseEntity.badRequest().body(problem);
        }

        if (input.getTitle() != null && input.getTitle().isBlank()) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                    "Title cannot be blank");
            problem.setTitle("Bad Request");
            return ResponseEntity.badRequest().body(problem);
        }
        if (input.getDurationMinutes() != null && input.getDurationMinutes() < 1) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                    "Duration must be at least 1 minute");
            problem.setTitle("Bad Request");
            return ResponseEntity.badRequest().body(problem);
        }

        return service.partialUpdateMovie(id, input)
                .map(o -> ResponseEntity.ok((Object) o))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String id) {
        if (service.deleteMovie(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
