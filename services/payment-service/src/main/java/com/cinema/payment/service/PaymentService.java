package com.cinema.payment.service;

import com.cinema.payment.dto.PaymentInput;
import com.cinema.payment.dto.PaymentOutput;
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
        if (repository.existsByBookingId(input.bookingId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment already exists for this booking");
        }

        String id = generateId();
        // Deterministic simulation: bookingId ends with "-FAIL" results in FAILED status
        String status = input.bookingId().endsWith("-FAIL") ? "FAILED" : "SUCCESS";
        
        Payment payment = new Payment(id, input.bookingId(), input.amount(), status);
        Payment saved = repository.save(payment);
        return mapToOutput(saved);
    }

    private String generateId() {
        long count = repository.count();
        return String.format("PY-%03d", count + 1);
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
