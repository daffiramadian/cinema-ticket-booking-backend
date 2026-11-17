package com.cinema.catalog.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    private String id;
    private String title;
    private Integer durationMinutes;
    private String description;

    @Column(length = 2000)
    private String synopsis;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @OrderColumn(name = "genre_order")
    @Column(name = "genre")
    private List<String> genres = new ArrayList<>();

    @Column(length = 500)
    private String posterUrl;

    @Column(length = 10)
    private String ageRating;

    @Column(length = 200)
    private String director;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"))
    @OrderColumn(name = "cast_order")
    @Column(name = "actor_name")
    private List<String> castMembers = new ArrayList<>();

    @Column(length = 500)
    private String trailerUrl;

    private LocalDate releaseDate;

    public Movie() {}

    public Movie(String id, String title, Integer durationMinutes, String description) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>(); }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public List<String> getCastMembers() { return castMembers; }
    public void setCastMembers(List<String> castMembers) { this.castMembers = castMembers != null ? new ArrayList<>(castMembers) : new ArrayList<>(); }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
}
