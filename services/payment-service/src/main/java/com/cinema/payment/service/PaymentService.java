package com.cinema.payment.service;

import com.cinema.payment.client.BookingClient;
import com.cinema.payment.dto.PaymentInput;
import com.cinema.payment.dto.PaymentOutput;
import com.cinema.payment.dto.PaymentV2Input;
import com.cinema.payment.entity.Payment;
import com.cinema.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository repository;
    private final BookingClient bookingClient;

    public PaymentService(PaymentRepository repository, BookingClient bookingClient) {
        this.repository = repository;
        this.bookingClient = bookingClient;
    }

    public Optional<PaymentOutput> getPaymentById(String id) {
        return repository.findById(id).map(this::mapToOutput);
    }

    @Transactional
    public PaymentOutput createPayment(PaymentInput input) {
        return savePayment(input.bookingId(), input.amount(), false);
    }

    @Transactional
    public PaymentOutput createPaymentV2(PaymentV2Input input) {
        return savePayment(input.bookingId(), input.amount(), true);
    }

    private PaymentOutput savePayment(String bookingId, BigDecimal amount, boolean confirmRemoteBooking) {
        if (repository.existsByBookingId(bookingId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment already exists for booking " + bookingId);
        }

        String status = bookingId.endsWith("-FAIL") ? "FAILED" : "SUCCESS";
        // Confirm remotely before saving SUCCESS so a failed Booking Service call leaves no local SUCCESS row.
        if (confirmRemoteBooking && "SUCCESS".equals(status)) {
            bookingClient.confirmBooking(bookingId);
        }

        String id = generateId();
        Payment payment = new Payment(id, bookingId, amount, status);
        Payment saved = repository.save(payment);
        log.info("Payment recorded: id={}, bookingId={}, status={}", saved.getId(), saved.getBookingId(), saved.getStatus());
        return mapToOutput(saved);
    }

    private String generateId() {
        int maxNum = repository.findAll().stream()
                .map(Payment::getId)
                .filter(id -> id != null && id.startsWith("PY-"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("PY-%03d", maxNum + 1);
    }

    private PaymentOutput mapToOutput(Payment payment) {
        return new PaymentOutput(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getStatus()
        );
    }
}
