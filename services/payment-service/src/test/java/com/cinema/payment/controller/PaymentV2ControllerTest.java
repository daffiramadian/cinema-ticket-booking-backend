package com.cinema.payment.controller;

import com.cinema.payment.dto.PaymentOutput;
import com.cinema.payment.dto.PaymentV2Input;
import com.cinema.payment.service.PaymentService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentV2Controller.class)
class PaymentV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPaymentV2_Valid_ReturnsCreated() throws Exception {
        PaymentV2Input input = new PaymentV2Input("BK-001", new BigDecimal("50000"));
        PaymentOutput output = new PaymentOutput("PY-001", "BK-001", new BigDecimal("50000"), "SUCCESS");
        when(service.createPaymentV2(any(PaymentV2Input.class))).thenReturn(output);

        mockMvc.perform(post("/api/v2/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("PY-001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void createPaymentV2_InvalidBookingIdFormat_ReturnsBadRequest() throws Exception {
        PaymentV2Input input = new PaymentV2Input("INVALID-1", new BigDecimal("50000"));

        mockMvc.perform(post("/api/v2/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createPaymentV2_Duplicate_ReturnsConflict() throws Exception {
        PaymentV2Input input = new PaymentV2Input("BK-001", new BigDecimal("50000"));
        when(service.createPaymentV2(any(PaymentV2Input.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Payment already exists for booking BK-001"));

        mockMvc.perform(post("/api/v2/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getPaymentByIdV2_NotFound_ReturnsProblemDetail() throws Exception {
        when(service.getPaymentById("PY-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v2/payments/PY-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Payment PY-999 not found"));
    }

    @Test
    void createPaymentV2_FailSuffix_ReturnsCreatedWithFailedStatus() throws Exception {
        PaymentV2Input input = new PaymentV2Input("BK-002-FAIL", new BigDecimal("50000"));
        PaymentOutput output = new PaymentOutput("PY-002", "BK-002-FAIL", new BigDecimal("50000"), "FAILED");
        when(service.createPaymentV2(any(PaymentV2Input.class))).thenReturn(output);

        mockMvc.perform(post("/api/v2/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("PY-002"))
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void createPaymentV2_BookingNotFound_ReturnsNotFound() throws Exception {
        PaymentV2Input input = new PaymentV2Input("BK-999", new BigDecimal("50000"));
        when(service.createPaymentV2(any(PaymentV2Input.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking BK-999 not found"));

        mockMvc.perform(post("/api/v2/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Booking BK-999 not found"));
    }

    @Test
    void createPaymentV2_BookingConflict_ReturnsConflict() throws Exception {
        PaymentV2Input input = new PaymentV2Input("BK-001", new BigDecimal("50000"));
        when(service.createPaymentV2(any(PaymentV2Input.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Booking BK-001 cannot be confirmed"));

        mockMvc.perform(post("/api/v2/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Booking BK-001 cannot be confirmed"));
    }

    @Test
    void createPaymentV2_BookingUnavailable_ReturnsServiceUnavailable() throws Exception {
        PaymentV2Input input = new PaymentV2Input("BK-001", new BigDecimal("50000"));
        when(service.createPaymentV2(any(PaymentV2Input.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Booking Service is unavailable, payment could not be confirmed"
                ));

        mockMvc.perform(post("/api/v2/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value("Booking Service is unavailable, payment could not be confirmed"));
    }
}
