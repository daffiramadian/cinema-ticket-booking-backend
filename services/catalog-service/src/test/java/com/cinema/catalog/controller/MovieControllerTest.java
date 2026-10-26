package com.cinema.catalog.controller;

import com.cinema.catalog.dto.MovieInput;
import com.cinema.catalog.dto.MovieOutput;
import com.cinema.catalog.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMovie_Valid_ReturnsCreated() throws Exception {
        MovieInput input = new MovieInput("Interstellar", 169, "Sci-fi movie");
        MovieOutput output = new MovieOutput("MV-001", "Interstellar", 169, "Sci-fi movie");

        when(service.createMovie(any(MovieInput.class))).thenReturn(output);

        mockMvc.perform(post("/api/v1/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("MV-001"))
                .andExpect(jsonPath("$.title").value("Interstellar"));
    }

    @Test
    void createMovie_Invalid_ReturnsBadRequest() throws Exception {
        MovieInput input = new MovieInput("", 0, "No title and zero duration");

        mockMvc.perform(post("/api/v1/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllMovies_ReturnsList() throws Exception {
        MovieOutput output = new MovieOutput("MV-001", "Interstellar", 169, "Sci-fi movie");
        when(service.getAllMovies()).thenReturn(List.of(output));

        mockMvc.perform(get("/api/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("MV-001"));
    }

    @Test
    void getMovieById_Exists_ReturnsOk() throws Exception {
        MovieOutput output = new MovieOutput("MV-001", "Interstellar", 169, "Sci-fi movie");
        when(service.getMovieById("MV-001")).thenReturn(Optional.of(output));

        mockMvc.perform(get("/api/v1/movies/MV-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MV-001"));
    }

    @Test
    void getMovieById_NotExists_ReturnsNotFound() throws Exception {
        when(service.getMovieById("MV-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/movies/MV-999"))
                .andExpect(status().isNotFound());
    }
}
