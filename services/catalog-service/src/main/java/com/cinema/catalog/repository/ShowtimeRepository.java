package com.cinema.catalog.repository;

import com.cinema.catalog.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, String> {
    List<Showtime> findByMovieIdOrderByStartsAtAsc(String movieId);
    boolean existsByMovieId(String movieId);
}
