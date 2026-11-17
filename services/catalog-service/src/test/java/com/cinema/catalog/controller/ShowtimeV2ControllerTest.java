package com.cinema.catalog.controller;

import com.cinema.catalog.dto.ShowtimeInput;
import com.cinema.catalog.dto.ShowtimeOutput;
import com.cinema.catalog.service.ShowtimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShowtimeV2Controller.class)
class ShowtimeV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShowtimeService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getShowtimesByMovie_ReturnsList() throws Exception {
        OffsetDateTime starts = OffsetDateTime.parse("2026-11-20T19:00:00+07:00");
        ShowtimeOutput out = new ShowtimeOutput("ST-001", "MV-001", "Cinema Malang Kota", "Studio 1", starts, new BigDecimal("50000"));
        when(service.getShowtimesByMovieId("MV-001")).thenReturn(List.of(out));

        mockMvc.perform(get("/api/v2/movies/MV-001/showtimes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("ST-001"))
                .andExpect(jsonPath("$[0].studioName").value("Studio 1"));
    }

    @Test
    void createShowtime_Valid_ReturnsCreated() throws Exception {
        OffsetDateTime starts = OffsetDateTime.parse("2026-11-20T19:00:00+07:00");
        ShowtimeInput input = new ShowtimeInput("MV-001", "Cinema Malang Kota", "Studio 1", starts, new BigDecimal("50000"));
        ShowtimeOutput out = new ShowtimeOutput("ST-001", "MV-001", "Cinema Malang Kota", "Studio 1", starts, new BigDecimal("50000"));
        when(service.createShowtime(any(ShowtimeInput.class))).thenReturn(out);

        mockMvc.perform(post("/api/v2/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ST-001"))
                .andExpect(jsonPath("$.movieId").value("MV-001"));
    }

    @Test
    void createShowtime_MovieNotFound_ReturnsNotFound() throws Exception {
        OffsetDateTime starts = OffsetDateTime.parse("2026-11-20T19:00:00+07:00");
        ShowtimeInput input = new ShowtimeInput("MV-999", "Cinema Malang Kota", "Studio 1", starts, new BigDecimal("50000"));
        when(service.createShowtime(any(ShowtimeInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie MV-999 not found"));

        mockMvc.perform(post("/api/v2/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getShowtimeById_Exists_ReturnsOk() throws Exception {
        OffsetDateTime starts = OffsetDateTime.parse("2026-11-20T19:00:00+07:00");
        ShowtimeOutput out = new ShowtimeOutput("ST-001", "MV-001", "Cinema Malang Kota", "Studio 1", starts, new BigDecimal("50000"));
        when(service.getShowtimeById("ST-001")).thenReturn(Optional.of(out));

        mockMvc.perform(get("/api/v2/showtimes/ST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ST-001"));
    }
}
