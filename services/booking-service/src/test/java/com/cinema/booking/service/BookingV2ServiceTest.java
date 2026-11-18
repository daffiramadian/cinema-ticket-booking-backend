package com.cinema.booking.service;

import com.cinema.booking.client.CatalogClient;
import com.cinema.booking.dto.BookingActionInput;
import com.cinema.booking.dto.BookingOutput;
import com.cinema.booking.entity.Booking;
import com.cinema.booking.entity.SeatAvailability;
import com.cinema.booking.repository.BookingRepository;
import com.cinema.booking.repository.SeatAvailabilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingV2ServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatAvailabilityRepository seatRepository;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private BookingV2Service service;

    @Test
    void applyBookingActionV2_CancelPendingBooking_ReleasesSeatAndSaves() {
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-11-20T18:15:00+07:00");
        Booking booking = new Booking("BK-001", "ST-001", "C08", "PENDING_PAYMENT", expiresAt);
        SeatAvailability seat = new SeatAvailability("ST-001", "C08", "C", 8, "HELD");
        seat.setBookingId("BK-001");
        seat.setHeldUntil(expiresAt);

        when(bookingRepository.findById("BK-001")).thenReturn(Optional.of(booking));
        when(seatRepository.findForUpdate("ST-001", "C08")).thenReturn(Optional.of(seat));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingOutput result = service.applyBookingActionV2("BK-001", new BookingActionInput("CANCEL"));

        verify(seatRepository).findForUpdate("ST-001", "C08");
        verify(bookingRepository).save(booking);
        verify(seatRepository).save(seat);
        assertEquals("CANCELLED", booking.getStatus());
        assertEquals("AVAILABLE", seat.getStatus());
        assertNull(seat.getBookingId());
        assertNull(seat.getHeldUntil());
        assertEquals("BK-001", result.id());
        assertEquals("CANCELLED", result.status());
        assertEquals(expiresAt, result.expiresAt());
    }

    @Test
    void applyBookingActionV2_SeatHeldByAnotherBooking_ThrowsConflictWithoutReleasingSeat() {
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-11-20T18:15:00+07:00");
        Booking booking = new Booking("BK-001", "ST-001", "C08", "PENDING_PAYMENT", expiresAt);
        SeatAvailability seat = new SeatAvailability("ST-001", "C08", "C", 8, "HELD");
        seat.setBookingId("BK-002");
        seat.setHeldUntil(expiresAt.plusMinutes(5));

        when(bookingRepository.findById("BK-001")).thenReturn(Optional.of(booking));
        when(seatRepository.findForUpdate("ST-001", "C08")).thenReturn(Optional.of(seat));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.applyBookingActionV2("BK-001", new BookingActionInput("CANCEL"))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("HELD", seat.getStatus());
        assertEquals("BK-002", seat.getBookingId());
        verify(bookingRepository, never()).save(any());
        verify(seatRepository, never()).save(any());
    }

    @Test
    void applyBookingActionV2_BookingAlreadyConfirmed_ThrowsConflict() {
        OffsetDateTime expiresAt = OffsetDateTime.parse("2026-11-20T18:15:00+07:00");
        Booking booking = new Booking("BK-001", "ST-001", "C08", "CONFIRMED", expiresAt);
        when(bookingRepository.findById("BK-001")).thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.applyBookingActionV2("BK-001", new BookingActionInput("CANCEL"))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(seatRepository, never()).findForUpdate(any(), any());
    }

    @Test
    void applyBookingActionV2_UnsupportedAction_ThrowsBadRequest() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.applyBookingActionV2("BK-001", new BookingActionInput("INVALID"))
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(bookingRepository, never()).findById(any());
    }

    @Test
    void applyBookingActionV2_ConfirmPendingBooking_BooksSeatClearsHeldUntilAndSaves() {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
        Booking booking = new Booking("BK-001", "ST-001", "C08", "PENDING_PAYMENT", expiresAt);
        SeatAvailability seat = new SeatAvailability("ST-001", "C08", "C", 8, "HELD");
        seat.setBookingId("BK-001");
        seat.setHeldUntil(expiresAt);

        when(bookingRepository.findById("BK-001")).thenReturn(Optional.of(booking));
        when(seatRepository.findForUpdate("ST-001", "C08")).thenReturn(Optional.of(seat));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingOutput result = service.applyBookingActionV2("BK-001", new BookingActionInput("CONFIRM"));

        verify(seatRepository).findForUpdate("ST-001", "C08");
        verify(bookingRepository).save(booking);
        verify(seatRepository).save(seat);
        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals("BOOKED", seat.getStatus());
        assertEquals("BK-001", seat.getBookingId());
        assertNull(seat.getHeldUntil());
        assertEquals("BK-001", result.id());
        assertEquals("CONFIRMED", result.status());
        assertEquals(expiresAt, result.expiresAt());
    }

    @Test
    void applyBookingActionV2_ConfirmMissingBooking_ThrowsNotFound() {
        when(bookingRepository.findById("BK-999")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.applyBookingActionV2("BK-999", new BookingActionInput("CONFIRM"))
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(seatRepository, never()).findForUpdate(any(), any());
    }

    @Test
    void applyBookingActionV2_ConfirmCancelledBooking_ThrowsConflict() {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
        Booking booking = new Booking("BK-001", "ST-001", "C08", "CANCELLED", expiresAt);
        when(bookingRepository.findById("BK-001")).thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.applyBookingActionV2("BK-001", new BookingActionInput("CONFIRM"))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(seatRepository, never()).findForUpdate(any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void applyBookingActionV2_ConfirmAlreadyConfirmedBooking_ThrowsConflict() {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
        Booking booking = new Booking("BK-001", "ST-001", "C08", "CONFIRMED", expiresAt);
        when(bookingRepository.findById("BK-001")).thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.applyBookingActionV2("BK-001", new BookingActionInput("CONFIRM"))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(seatRepository, never()).findForUpdate(any(), any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void applyBookingActionV2_ConfirmExpiredBooking_ThrowsConflict() {
        OffsetDateTime expiredAt = OffsetDateTime.now().minusMinutes(1);
        Booking booking = new Booking("BK-001", "ST-001", "C08", "PENDING_PAYMENT", expiredAt);
        SeatAvailability seat = new SeatAvailability("ST-001", "C08", "C", 8, "HELD");
        seat.setBookingId("BK-001");
        seat.setHeldUntil(expiredAt);

        when(bookingRepository.findById("BK-001")).thenReturn(Optional.of(booking));
        when(seatRepository.findForUpdate("ST-001", "C08")).thenReturn(Optional.of(seat));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.applyBookingActionV2("BK-001", new BookingActionInput("CONFIRM"))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("PENDING_PAYMENT", booking.getStatus());
        assertEquals("HELD", seat.getStatus());
        assertEquals(expiredAt, seat.getHeldUntil());
        verify(bookingRepository, never()).save(any());
        verify(seatRepository, never()).save(any());
    }

    @Test
    void applyBookingActionV2_ConfirmSeatHeldByAnotherBooking_ThrowsConflictWithoutModifyingSeat() {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
        Booking booking = new Booking("BK-001", "ST-001", "C08", "PENDING_PAYMENT", expiresAt);
        SeatAvailability seat = new SeatAvailability("ST-001", "C08", "C", 8, "HELD");
        seat.setBookingId("BK-002");
        seat.setHeldUntil(expiresAt.plusMinutes(5));

        when(bookingRepository.findById("BK-001")).thenReturn(Optional.of(booking));
        when(seatRepository.findForUpdate("ST-001", "C08")).thenReturn(Optional.of(seat));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.applyBookingActionV2("BK-001", new BookingActionInput("CONFIRM"))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("HELD", seat.getStatus());
        assertEquals("BK-002", seat.getBookingId());
        assertEquals(expiresAt.plusMinutes(5), seat.getHeldUntil());
        verify(bookingRepository, never()).save(any());
        verify(seatRepository, never()).save(any());
    }
}
