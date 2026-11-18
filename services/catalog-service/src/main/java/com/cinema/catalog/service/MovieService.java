package com.cinema.catalog.service;

import com.cinema.catalog.dto.MovieInput;
import com.cinema.catalog.dto.MovieOutput;
import com.cinema.catalog.dto.MoviePatchInput;
import com.cinema.catalog.dto.MovieV2Input;
import com.cinema.catalog.dto.MovieV2Output;
import com.cinema.catalog.dto.MovieV2PatchInput;
import com.cinema.catalog.entity.Movie;
import com.cinema.catalog.repository.MovieRepository;
import com.cinema.catalog.repository.ShowtimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository repository;
    private final ShowtimeRepository showtimeRepository;

    public MovieService(MovieRepository repository, ShowtimeRepository showtimeRepository) {
        this.repository = repository;
        this.showtimeRepository = showtimeRepository;
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
        movie.setSynopsis(input.description());
        Movie saved = repository.save(movie);
        return mapToOutput(saved);
    }

    @Transactional
    public Optional<MovieOutput> updateMovie(String id, MovieInput input) {
        return repository.findById(id).map(movie -> {
            movie.setTitle(input.title());
            movie.setDurationMinutes(input.durationMinutes());
            movie.setDescription(input.description());
            movie.setSynopsis(input.description());
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
                movie.setSynopsis(input.getDescription());
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

    // === API v2.0 Operations (GET & POST) ===

    public List<MovieV2Output> getAllMoviesV2() {
        return repository.findAll().stream()
                .map(this::mapToV2Output)
                .toList();
    }

    public Optional<MovieV2Output> getMovieByIdV2(String id) {
        return repository.findById(id).map(this::mapToV2Output);
    }

    @Transactional
    public MovieV2Output createMovieV2(MovieV2Input input) {
        String id = generateId();
        Movie movie = new Movie(id, input.title(), input.durationMinutes(), input.synopsis());
        movie.setSynopsis(input.synopsis());
        movie.setGenres(input.genres());
        movie.setPosterUrl(input.posterUrl());
        movie.setAgeRating(input.ageRating());
        movie.setDirector(input.director());
        movie.setCastMembers(input.cast());
        movie.setTrailerUrl(input.trailerUrl());
        movie.setReleaseDate(input.releaseDate());
        Movie saved = repository.save(movie);
        return mapToV2Output(saved);
    }

    // === API v2.0 Operations (PUT, PATCH, DELETE) ===

    @Transactional
    public Optional<MovieV2Output> replaceMovieV2(String id, MovieV2Input input) {
        return repository.findById(id).map(movie -> {
            movie.setTitle(input.title());
            movie.setSynopsis(input.synopsis());
            movie.setDescription(input.synopsis());
            movie.setDurationMinutes(input.durationMinutes());
            movie.setGenres(input.genres());
            movie.setPosterUrl(input.posterUrl());
            movie.setAgeRating(input.ageRating());
            movie.setDirector(input.director());
            movie.setCastMembers(input.cast());
            movie.setTrailerUrl(input.trailerUrl());
            movie.setReleaseDate(input.releaseDate());
            return mapToV2Output(repository.save(movie));
        });
    }

    @Transactional
    public Optional<MovieV2Output> partialUpdateMovieV2(String id, MovieV2PatchInput input) {
        return repository.findById(id).map(movie -> {
            if (input.title() != null) {
                movie.setTitle(input.title());
            }
            if (input.synopsis() != null) {
                movie.setSynopsis(input.synopsis());
                movie.setDescription(input.synopsis());
            }
            if (input.durationMinutes() != null) {
                movie.setDurationMinutes(input.durationMinutes());
            }
            if (input.genres() != null) {
                movie.setGenres(input.genres());
            }
            if (input.posterUrl() != null) {
                movie.setPosterUrl(input.posterUrl());
            }
            if (input.ageRating() != null) {
                movie.setAgeRating(input.ageRating());
            }
            if (input.director() != null) {
                movie.setDirector(input.director());
            }
            if (input.cast() != null) {
                movie.setCastMembers(input.cast());
            }
            if (input.trailerUrl() != null) {
                movie.setTrailerUrl(input.trailerUrl());
            }
            if (input.releaseDate() != null) {
                movie.setReleaseDate(input.releaseDate());
            }
            return mapToV2Output(repository.save(movie));
        });
    }

    @Transactional
    public void deleteMovieV2(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie " + id + " not found");
        }
        if (showtimeRepository.existsByMovieId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Movie " + id + " still has showtimes");
        }
        repository.deleteById(id);
    }

    private String generateId() {
        int maxNum = repository.findAll().stream()
                .map(Movie::getId)
                .filter(id -> id != null && id.startsWith("MV-"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("MV-%03d", maxNum + 1);
    }

    private MovieOutput mapToOutput(Movie movie) {
        return new MovieOutput(movie.getId(), movie.getTitle(), movie.getDurationMinutes(), movie.getDescription());
    }

    private MovieV2Output mapToV2Output(Movie movie) {
        String effectiveSynopsis = movie.getSynopsis() != null ? movie.getSynopsis() : movie.getDescription();
        return new MovieV2Output(
                movie.getId(),
                movie.getTitle(),
                effectiveSynopsis,
                movie.getDurationMinutes(),
                movie.getGenres() != null ? List.copyOf(movie.getGenres()) : List.of(),
                movie.getPosterUrl(),
                movie.getAgeRating(),
                movie.getDirector(),
                movie.getCastMembers() != null ? List.copyOf(movie.getCastMembers()) : List.of(),
                movie.getTrailerUrl(),
                movie.getReleaseDate()
        );
    }
}
