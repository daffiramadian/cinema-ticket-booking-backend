package com.cinema.booking.controller;

import com.cinema.booking.dto.BookingActionInput;
import com.cinema.booking.dto.BookingOutput;
import com.cinema.booking.dto.BookingV2Input;
import com.cinema.booking.dto.SeatAvailabilityOutput;
import com.cinema.booking.service.BookingV2Service;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingV2Controller.class)
class BookingV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingV2Service service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getSeatsByShowtime_ReturnsSeatMap() throws Exception {
        List<SeatAvailabilityOutput> seats = List.of(
                new SeatAvailabilityOutput("C07", "C", 7, "AVAILABLE"),
                new SeatAvailabilityOutput("C08", "C", 8, "HELD")
        );
        when(service.getSeatsByShowtime("ST-001")).thenReturn(seats);

        mockMvc.perform(get("/api/v2/showtimes/ST-001/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("C07"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[1].seatNumber").value("C08"))
                .andExpect(jsonPath("$[1].status").value("HELD"));
    }

    @Test
    void createBookingV2_Valid_ReturnsCreated() throws Exception {
        BookingV2Input input = new BookingV2Input("ST-001", "C08");
        BookingOutput output = new BookingOutput("BK-001", "ST-001", "C08", "PENDING_PAYMENT", OffsetDateTime.now().plusMinutes(15));
        when(service.createBookingV2(any(BookingV2Input.class))).thenReturn(output);

        mockMvc.perform(post("/api/v2/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("BK-001"))
                .andExpect(jsonPath("$.seatNumber").value("C08"))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }

    @Test
    void createBookingV2_InvalidSeatPattern_ReturnsBadRequest() throws Exception {
        BookingV2Input input = new BookingV2Input("ST-001", "Z99");

        mockMvc.perform(post("/api/v2/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createBookingV2_SeatHeld_ReturnsConflict() throws Exception {
        BookingV2Input input = new BookingV2Input("ST-001", "C08");
        when(service.createBookingV2(any(BookingV2Input.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Seat C08 is not available for showtime ST-001"));

        mockMvc.perform(post("/api/v2/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getBookingByIdV2_NotFound_ReturnsProblemDetail() throws Exception {
        when(service.getBookingByIdV2("BK-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v2/bookings/BK-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Booking BK-999 not found"));
    }

    @Test
    void applyBookingActionV2_Cancel_ReturnsOk() throws Exception {
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-11-20T18:15:00+07:00");
        BookingOutput output = new BookingOutput("BK-001", "ST-001", "C08", "CANCELLED", expiresAt);
        when(service.applyBookingActionV2(eq("BK-001"), any(BookingActionInput.class))).thenReturn(output);

        mockMvc.perform(patch("/api/v2/bookings/BK-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CANCEL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("BK-001"))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void applyBookingActionV2_EmptyAction_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v2/bookings/BK-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void applyBookingActionV2_UnsupportedAction_ReturnsBadRequest() throws Exception {
        when(service.applyBookingActionV2(eq("BK-001"), any(BookingActionInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported action: INVALID"));

        mockMvc.perform(patch("/api/v2/bookings/BK-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Unsupported action: INVALID"));
    }

    @Test
    void applyBookingActionV2_Confirm_ReturnsOk() throws Exception {
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-11-20T18:15:00+07:00");
        BookingOutput output = new BookingOutput("BK-001", "ST-001", "C08", "CONFIRMED", expiresAt);
        when(service.applyBookingActionV2(eq("BK-001"), any(BookingActionInput.class))).thenReturn(output);

        mockMvc.perform(patch("/api/v2/bookings/BK-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CONFIRM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("BK-001"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void applyBookingActionV2_ConfirmMissingBooking_ReturnsNotFound() throws Exception {
        when(service.applyBookingActionV2(eq("BK-999"), any(BookingActionInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking BK-999 not found"));

        mockMvc.perform(patch("/api/v2/bookings/BK-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CONFIRM\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Booking BK-999 not found"));
    }

    @Test
    void applyBookingActionV2_ConfirmCancelledBooking_ReturnsConflict() throws Exception {
        when(service.applyBookingActionV2(eq("BK-001"), any(BookingActionInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Cannot confirm booking BK-001 in status CANCELLED"));

        mockMvc.perform(patch("/api/v2/bookings/BK-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CONFIRM\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Cannot confirm booking BK-001 in status CANCELLED"));
    }

    @Test
    void applyBookingActionV2_ConfirmExpiredBooking_ReturnsConflict() throws Exception {
        when(service.applyBookingActionV2(eq("BK-001"), any(BookingActionInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Booking BK-001 hold has expired"));

        mockMvc.perform(patch("/api/v2/bookings/BK-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CONFIRM\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Booking BK-001 hold has expired"));
    }

    @Test
    void applyBookingActionV2_MissingBooking_ReturnsNotFound() throws Exception {
        when(service.applyBookingActionV2(eq("BK-999"), any(BookingActionInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking BK-999 not found"));

        mockMvc.perform(patch("/api/v2/bookings/BK-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CANCEL\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Booking BK-999 not found"));
    }

    @Test
    void applyBookingActionV2_AlreadyCancelled_ReturnsConflict() throws Exception {
        when(service.applyBookingActionV2(eq("BK-001"), any(BookingActionInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Booking BK-001 is already CANCELLED"));

        mockMvc.perform(patch("/api/v2/bookings/BK-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CANCEL\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Booking BK-001 is already CANCELLED"));
    }

    @Test
    void applyBookingActionV2_AlreadyConfirmed_ReturnsConflict() throws Exception {
        when(service.applyBookingActionV2(eq("BK-001"), any(BookingActionInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel booking BK-001 in status CONFIRMED"));

        mockMvc.perform(patch("/api/v2/bookings/BK-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"CANCEL\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Cannot cancel booking BK-001 in status CONFIRMED"));
    }
}
