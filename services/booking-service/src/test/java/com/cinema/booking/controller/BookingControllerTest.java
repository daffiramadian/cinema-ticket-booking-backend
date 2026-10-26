package com.cinema.booking.controller;

import com.cinema.booking.dto.BookingInput;
import com.cinema.booking.dto.BookingOutput;
import com.cinema.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_Valid_ReturnsCreated() throws Exception {
        BookingInput input = new BookingInput("ST-001", "C08");
        BookingOutput output = new BookingOutput("BK-001", "ST-001", "C08", "PENDING_PAYMENT", OffsetDateTime.now().plusMinutes(15));

        when(service.createBooking(any(BookingInput.class))).thenReturn(output);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("BK-001"))
                .andExpect(jsonPath("$.showtimeId").value("ST-001"))
                .andExpect(jsonPath("$.seatNumber").value("C08"))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void createBooking_Invalid_ReturnsBadRequest() throws Exception {
        BookingInput input = new BookingInput("", ""); // Both blank

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createBooking(any());
    }

    @Test
    void getBookingById_Exists_ReturnsOk() throws Exception {
        BookingOutput output = new BookingOutput("BK-001", "ST-001", "C08", "PENDING_PAYMENT", OffsetDateTime.now().plusMinutes(15));
        when(service.getBookingById("BK-001")).thenReturn(Optional.of(output));

        mockMvc.perform(get("/api/v1/bookings/BK-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("BK-001"));
    }

    @Test
    void getBookingById_NotExists_ReturnsNotFound() throws Exception {
        when(service.getBookingById("BK-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/bookings/BK-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_Duplicate_ReturnsConflict() throws Exception {
        BookingInput input = new BookingInput("ST-001", "C08");
        when(service.createBooking(any(BookingInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Seat already booked for this showtime"));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict());
    }
}
