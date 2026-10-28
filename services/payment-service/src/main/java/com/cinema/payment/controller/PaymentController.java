package com.cinema.payment.controller;

import com.cinema.payment.dto.PaymentInput;
import com.cinema.payment.dto.PaymentOutput;
import com.cinema.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentOutput createPayment(@Valid @RequestBody PaymentInput input) {
        return service.createPayment(input);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentOutput> getPaymentById(@PathVariable String id) {
        Optional<PaymentOutput> opt = service.getPaymentById(id);
        return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
