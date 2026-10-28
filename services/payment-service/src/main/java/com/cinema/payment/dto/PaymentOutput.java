package com.cinema.payment.dto;

import java.math.BigDecimal;

public record PaymentOutput(
    String id,
    String bookingId,
    BigDecimal amount,
    String status
) {}
