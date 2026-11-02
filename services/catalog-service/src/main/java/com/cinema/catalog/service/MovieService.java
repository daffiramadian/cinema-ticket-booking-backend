package com.cinema.catalog.service;

import com.cinema.catalog.dto.MovieInput;
import com.cinema.catalog.dto.MovieOutput;
import com.cinema.catalog.dto.MoviePatchInput;
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

    @Transactional
    public Optional<MovieOutput> updateMovie(String id, MovieInput input) {
        return repository.findById(id).map(movie -> {
            movie.setTitle(input.title());
            movie.setDurationMinutes(input.durationMinutes());
            movie.setDescription(input.description());
            return mapToOutput(repository.save(movie));
        });
    }

    @Transactional
    public Optional<MovieOutput> partialUpdateMovie(String id, MoviePatchInput input) {
        return repository.findById(id).map(movie -> {
            if (input.getTitle() != null) {
                movie.setTitle(input.getTitle());
            }
            if (input.getDurationMinutes() != null) {
                movie.setDurationMinutes(input.getDurationMinutes());
            }
            if (input.getDescription() != null) {
                movie.setDescription(input.getDescription());
            }
            return mapToOutput(repository.save(movie));
        });
    }

    @Transactional
    public boolean deleteMovie(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    private String generateId() {
        long count = repository.count();
        return String.format("MV-%03d", count + 1);
    }

    private MovieOutput mapToOutput(Movie movie) {
        return new MovieOutput(movie.getId(), movie.getTitle(), movie.getDurationMinutes(), movie.getDescription());
    }
}
