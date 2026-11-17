package com.cinema.catalog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "showtimes")
public class Showtime {
    @Id
    private String id;
    private String movieId;
    private String cinemaName;
    private String studioName;
    private OffsetDateTime startsAt;
    private BigDecimal price;

    public Showtime() {}

    public Showtime(String id, String movieId, String cinemaName, String studioName, OffsetDateTime startsAt, BigDecimal price) {
        this.id = id;
        this.movieId = movieId;
        this.cinemaName = cinemaName;
        this.studioName = studioName;
        this.startsAt = startsAt;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }
    public String getStudioName() { return studioName; }
    public void setStudioName(String studioName) { this.studioName = studioName; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(OffsetDateTime startsAt) { this.startsAt = startsAt; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
