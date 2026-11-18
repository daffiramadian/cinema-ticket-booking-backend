package com.cinema.payment.service;

import com.cinema.payment.client.BookingClient;
import com.cinema.payment.dto.PaymentInput;
import com.cinema.payment.dto.PaymentOutput;
import com.cinema.payment.dto.PaymentV2Input;
import com.cinema.payment.entity.Payment;
import com.cinema.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository repository;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private PaymentService service;

    @Test
    void createPaymentV2_Valid_ConfirmsBookingBeforeSavingSuccess() {
        when(repository.existsByBookingId("BK-001")).thenReturn(false);
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentOutput result = service.createPaymentV2(new PaymentV2Input("BK-001", new BigDecimal("50000")));

        InOrder order = inOrder(bookingClient, repository);
        order.verify(repository).existsByBookingId("BK-001");
        order.verify(bookingClient).confirmBooking("BK-001");
        order.verify(repository).save(any(Payment.class));

        assertEquals("PY-001", result.id());
        assertEquals("BK-001", result.bookingId());
        assertEquals(new BigDecimal("50000"), result.amount());
        assertEquals("SUCCESS", result.status());
    }

    @Test
    void createPaymentV2_DeterministicFail_SavesFailedWithoutCallingBookingService() {
        when(repository.existsByBookingId("BK-002-FAIL")).thenReturn(false);
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentOutput result = service.createPaymentV2(new PaymentV2Input("BK-002-FAIL", new BigDecimal("50000")));

        verify(bookingClient, never()).confirmBooking(any());
        verify(repository).save(any(Payment.class));
        assertEquals("FAILED", result.status());
        assertEquals("BK-002-FAIL", result.bookingId());
    }

    @Test
    void createPaymentV2_BookingNotFound_ThrowsNotFoundWithoutSaving() {
        when(repository.existsByBookingId("BK-999")).thenReturn(false);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking BK-999 not found"))
                .when(bookingClient).confirmBooking("BK-999");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.createPaymentV2(new PaymentV2Input("BK-999", new BigDecimal("50000")))
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void createPaymentV2_BookingConflict_ThrowsConflictWithoutSaving() {
        when(repository.existsByBookingId("BK-001")).thenReturn(false);
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Booking BK-001 cannot be confirmed"))
                .when(bookingClient).confirmBooking("BK-001");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.createPaymentV2(new PaymentV2Input("BK-001", new BigDecimal("50000")))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void createPaymentV2_BookingUnavailable_ThrowsServiceUnavailableWithoutSaving() {
        when(repository.existsByBookingId("BK-001")).thenReturn(false);
        doThrow(new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Booking Service is unavailable, payment could not be confirmed"
        )).when(bookingClient).confirmBooking("BK-001");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.createPaymentV2(new PaymentV2Input("BK-001", new BigDecimal("50000")))
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void createPaymentV2_DuplicatePayment_ThrowsConflictWithoutCallingBookingService() {
        when(repository.existsByBookingId("BK-001")).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.createPaymentV2(new PaymentV2Input("BK-001", new BigDecimal("50000")))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(bookingClient, never()).confirmBooking(any());
        verify(repository, never()).save(any());
    }

    @Test
    void createPaymentV1_Valid_DoesNotCallBookingService() {
        when(repository.existsByBookingId("BK-001")).thenReturn(false);
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentOutput result = service.createPayment(new PaymentInput("BK-001", new BigDecimal("50000")));

        verify(bookingClient, never()).confirmBooking(any());
        verify(repository).save(any(Payment.class));
        assertEquals("SUCCESS", result.status());
    }
}
