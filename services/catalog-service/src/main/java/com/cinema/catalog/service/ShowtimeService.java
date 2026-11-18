package com.cinema.catalog.service;

import com.cinema.catalog.dto.ShowtimeInput;
import com.cinema.catalog.dto.ShowtimeOutput;
import com.cinema.catalog.entity.Showtime;
import com.cinema.catalog.repository.MovieRepository;
import com.cinema.catalog.repository.ShowtimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
    }

    public List<ShowtimeOutput> getShowtimesByMovieId(String movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie " + movieId + " not found");
        }
        return showtimeRepository.findByMovieIdOrderByStartsAtAsc(movieId).stream()
                .map(this::mapToOutput)
                .toList();
    }

    public Optional<ShowtimeOutput> getShowtimeById(String id) {
        return showtimeRepository.findById(id).map(this::mapToOutput);
    }

    @Transactional
    public ShowtimeOutput createShowtime(ShowtimeInput input) {
        if (!movieRepository.existsById(input.movieId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie " + input.movieId() + " not found");
        }
        String id = generateId();
        Showtime showtime = new Showtime(
                id,
                input.movieId(),
                input.cinemaName(),
                input.studioName(),
                input.startsAt(),
                input.price()
        );
        return mapToOutput(showtimeRepository.save(showtime));
    }

    @Transactional
    public ShowtimeOutput replaceShowtime(String id, ShowtimeInput input) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime " + id + " not found"));
        if (!movieRepository.existsById(input.movieId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie " + input.movieId() + " not found");
        }
        showtime.setMovieId(input.movieId());
        showtime.setCinemaName(input.cinemaName());
        showtime.setStudioName(input.studioName());
        showtime.setStartsAt(input.startsAt());
        showtime.setPrice(input.price());
        return mapToOutput(showtimeRepository.save(showtime));
    }

    @Transactional
    public void deleteShowtime(String id) {
        if (!showtimeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime " + id + " not found");
        }
        showtimeRepository.deleteById(id);
    }

    private String generateId() {
        int maxNum = showtimeRepository.findAll().stream()
                .map(Showtime::getId)
                .filter(id -> id != null && id.startsWith("ST-"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("ST-%03d", maxNum + 1);
    }

    private ShowtimeOutput mapToOutput(Showtime showtime) {
        return new ShowtimeOutput(
                showtime.getId(),
                showtime.getMovieId(),
                showtime.getCinemaName(),
                showtime.getStudioName(),
                showtime.getStartsAt(),
                showtime.getPrice()
        );
    }
}
