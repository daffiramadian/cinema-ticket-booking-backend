package com.cinema.payment.service;

import com.cinema.payment.dto.PaymentInput;
import com.cinema.payment.dto.PaymentOutput;
import com.cinema.payment.dto.PaymentV2Input;
import com.cinema.payment.entity.Payment;
import com.cinema.payment.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public Optional<PaymentOutput> getPaymentById(String id) {
        return repository.findById(id).map(this::mapToOutput);
    }

    @Transactional
    public PaymentOutput createPayment(PaymentInput input) {
        return processPayment(input.bookingId(), input.amount());
    }

    @Transactional
    public PaymentOutput createPaymentV2(PaymentV2Input input) {
        return processPayment(input.bookingId(), input.amount());
    }

    private PaymentOutput processPayment(String bookingId, java.math.BigDecimal amount) {
        if (repository.existsByBookingId(bookingId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment already exists for booking " + bookingId);
        }

        String id = generateId();
        String status = bookingId.endsWith("-FAIL") ? "FAILED" : "SUCCESS";

        Payment payment = new Payment(id, bookingId, amount, status);
        Payment saved = repository.save(payment);
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
