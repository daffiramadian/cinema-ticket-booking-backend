package com.cinema.catalog.service;

import com.cinema.catalog.dto.MovieInput;
import com.cinema.catalog.dto.MovieOutput;
import com.cinema.catalog.entity.Movie;
import com.cinema.catalog.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository repository;

    public MovieService(MovieRepository repository) {
        this.repository = repository;
    }

    public List<MovieOutput> getAllMovies() {
        return repository.findAll().stream()
                .map(this::mapToOutput)
                .toList();
    }

    public Optional<MovieOutput> getMovieById(String id) {
        return repository.findById(id).map(this::mapToOutput);
    }

    @Transactional
    public MovieOutput createMovie(MovieInput input) {
        String id = generateId();
        Movie movie = new Movie(id, input.title(), input.durationMinutes(), input.description());
        Movie saved = repository.save(movie);
        return mapToOutput(saved);
    }

    private String generateId() {
        long count = repository.count();
        return String.format("MV-%03d", count + 1);
    }

    private MovieOutput mapToOutput(Movie movie) {
        return new MovieOutput(movie.getId(), movie.getTitle(), movie.getDurationMinutes(), movie.getDescription());
    }
}
