package com.cinema.catalog.controller;

import com.cinema.catalog.dto.MovieV2Input;
import com.cinema.catalog.dto.MovieV2Output;
import com.cinema.catalog.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieV2Controller.class)
class MovieV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllMoviesV2_ReturnsList() throws Exception {
        MovieV2Output output = new MovieV2Output(
                "MV-001", "Interstellar", "Perjalanan luar angkasa.", 169,
                List.of("Sci-Fi", "Adventure"), "https://images.example.com/posters/interstellar.jpg",
                "13+", "Christopher Nolan", List.of("Matthew McConaughey"),
                "https://videos.example.com/trailers/interstellar", LocalDate.of(2014, 11, 7)
        );
        when(service.getAllMoviesV2()).thenReturn(List.of(output));

        mockMvc.perform(get("/api/v2/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("MV-001"))
                .andExpect(jsonPath("$[0].synopsis").value("Perjalanan luar angkasa."))
                .andExpect(jsonPath("$[0].genres[0]").value("Sci-Fi"))
                .andExpect(jsonPath("$[0].posterUrl").value("https://images.example.com/posters/interstellar.jpg"));
    }

    @Test
    void getMovieByIdV2_NotFound_ReturnsProblemDetail() throws Exception {
        when(service.getMovieByIdV2("MV-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v2/movies/MV-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Movie MV-999 not found"));
    }

    @Test
    void createMovieV2_Valid_ReturnsCreated() throws Exception {
        MovieV2Input input = new MovieV2Input(
                "Interstellar", "Perjalanan luar angkasa.", 169,
                List.of("Sci-Fi", "Adventure"), "https://images.example.com/posters/interstellar.jpg",
                "13+", "Christopher Nolan", List.of("Matthew McConaughey"),
                "https://videos.example.com/trailers/interstellar", LocalDate.of(2014, 11, 7)
        );
        MovieV2Output output = new MovieV2Output(
                "MV-001", "Interstellar", "Perjalanan luar angkasa.", 169,
                List.of("Sci-Fi", "Adventure"), "https://images.example.com/posters/interstellar.jpg",
                "13+", "Christopher Nolan", List.of("Matthew McConaughey"),
                "https://videos.example.com/trailers/interstellar", LocalDate.of(2014, 11, 7)
        );
        when(service.createMovieV2(any(MovieV2Input.class))).thenReturn(output);

        mockMvc.perform(post("/api/v2/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("MV-001"))
                .andExpect(jsonPath("$.posterUrl").value("https://images.example.com/posters/interstellar.jpg"));
    }

    @Test
    void createMovieV2_NonHttpsPoster_ReturnsBadRequest() throws Exception {
        MovieV2Input input = new MovieV2Input(
                "Interstellar", "Perjalanan luar angkasa.", 169,
                List.of("Sci-Fi"), "http://insecure.example.com/poster.jpg",
                null, null, null, null, null
        );

        mockMvc.perform(post("/api/v2/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("posterUrl: must be a valid https:// URL"));
    }
}
