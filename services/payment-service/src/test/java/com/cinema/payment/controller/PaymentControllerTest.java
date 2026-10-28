package com.cinema.payment.controller;

import com.cinema.payment.dto.PaymentInput;
import com.cinema.payment.dto.PaymentOutput;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPayment_Valid_ReturnsCreated() throws Exception {
        PaymentInput input = new PaymentInput("BK-001", new BigDecimal("50000"));
        PaymentOutput output = new PaymentOutput("PY-001", "BK-001", new BigDecimal("50000"), "SUCCESS");

        when(service.createPayment(any(PaymentInput.class))).thenReturn(output);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("PY-001"))
                .andExpect(jsonPath("$.bookingId").value("BK-001"))
                .andExpect(jsonPath("$.amount").value(50000))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void createPayment_Invalid_ReturnsBadRequest() throws Exception {
        PaymentInput input = new PaymentInput("", new BigDecimal("-10")); // Invalid

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createPayment(any());
    }

    @Test
    void getPaymentById_Exists_ReturnsOk() throws Exception {
        PaymentOutput output = new PaymentOutput("PY-001", "BK-001", new BigDecimal("50000"), "SUCCESS");
        when(service.getPaymentById("PY-001")).thenReturn(Optional.of(output));

        mockMvc.perform(get("/api/v1/payments/PY-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PY-001"));
    }

    @Test
    void getPaymentById_NotExists_ReturnsNotFound() throws Exception {
        when(service.getPaymentById("PY-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/payments/PY-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPayment_Duplicate_ReturnsConflict() throws Exception {
        PaymentInput input = new PaymentInput("BK-001", new BigDecimal("50000"));
        when(service.createPayment(any(PaymentInput.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Payment already exists for this booking"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict());
    }

    @Test
    void createPayment_FailScenario_ReturnsSuccessWithFailedStatus() throws Exception {
        // Test deterministic failure rule: bookingId ends with "-FAIL"
        PaymentInput input = new PaymentInput("BK-001-FAIL", new BigDecimal("50000"));
        PaymentOutput output = new PaymentOutput("PY-002", "BK-001-FAIL", new BigDecimal("50000"), "FAILED");

        when(service.createPayment(any(PaymentInput.class))).thenReturn(output);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }
}
