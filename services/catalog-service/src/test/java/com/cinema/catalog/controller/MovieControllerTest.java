package com.cinema.catalog.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cinema.catalog.dto.MovieInput;
import com.cinema.catalog.dto.MovieOutput;
import com.cinema.catalog.dto.MoviePatchInput;
import com.cinema.catalog.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Test
    void replaceMovie_Valid_ReturnsOk() throws Exception {
        MovieInput input = new MovieInput("Interstellar Remastered", 180, "Updated desc");
        MovieOutput output = new MovieOutput("MV-001", "Interstellar Remastered", 180, "Updated desc");

        when(service.updateMovie(eq("MV-001"), any(MovieInput.class))).thenReturn(Optional.of(output));

        mockMvc.perform(put("/api/v1/movies/MV-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MV-001"))
                .andExpect(jsonPath("$.title").value("Interstellar Remastered"))
                .andExpect(jsonPath("$.durationMinutes").value(180));
    }

    @Test
    void replaceMovie_NotFound_ReturnsNotFound() throws Exception {
        MovieInput input = new MovieInput("Interstellar", 169, null);

        when(service.updateMovie(eq("MV-999"), any(MovieInput.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/movies/MV-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void replaceMovie_InvalidInput_ReturnsBadRequest() throws Exception {
        MovieInput input = new MovieInput("", -1, null);

        mockMvc.perform(put("/api/v1/movies/MV-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMovie_OneField_UpdatesAndPreservesOthers() throws Exception {
        MoviePatchInput input = new MoviePatchInput();
        input.setTitle("New Title");

        MovieOutput output = new MovieOutput("MV-001", "New Title", 169, "Sci-fi movie");

        when(service.partialUpdateMovie(eq("MV-001"), any(MoviePatchInput.class))).thenReturn(Optional.of(output));

        mockMvc.perform(patch("/api/v1/movies/MV-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.durationMinutes").value(169));
    }

    @Test
    void updateMovie_NoFields_ReturnsBadRequest() throws Exception {
        MoviePatchInput input = new MoviePatchInput();

        mockMvc.perform(patch("/api/v1/movies/MV-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMovie_InvalidTitle_ReturnsBadRequest() throws Exception {
        MoviePatchInput input = new MoviePatchInput();
        input.setTitle("   ");

        mockMvc.perform(patch("/api/v1/movies/MV-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMovie_NotFound_ReturnsNotFound() throws Exception {
        MoviePatchInput input = new MoviePatchInput();
        input.setTitle("Ghost");

        when(service.partialUpdateMovie(eq("MV-999"), any(MoviePatchInput.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/movies/MV-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMovie_Exists_ReturnsNoContent() throws Exception {
        when(service.deleteMovie("MV-001")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/movies/MV-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMovie_NotFound_ReturnsNotFound() throws Exception {
        when(service.deleteMovie("MV-999")).thenReturn(false);

        mockMvc.perform(delete("/api/v1/movies/MV-999"))
                .andExpect(status().isNotFound());
    }
}
