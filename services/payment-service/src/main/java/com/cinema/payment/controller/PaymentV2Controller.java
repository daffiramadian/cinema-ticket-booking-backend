package com.cinema.payment.controller;

import com.cinema.payment.dto.PaymentOutput;
import com.cinema.payment.dto.PaymentV2Input;
import com.cinema.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v2/payments")
public class PaymentV2Controller {
    private final PaymentService service;

    public PaymentV2Controller(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentOutput createPayment(@Valid @RequestBody PaymentV2Input input) {
        return service.createPaymentV2(input);
    }

    @GetMapping("/{id}")
    public PaymentOutput getPaymentById(@PathVariable String id) {
        return service.getPaymentById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment " + id + " not found"));
    }
}
