package com.cinema.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PaymentV2Input(
        @NotBlank(message = "must not be blank")
        @Pattern(regexp = "^BK-[0-9]{3,}(-FAIL)?$", message = "must match BK-NNN or BK-NNN-FAIL")
        String bookingId,

        @NotNull(message = "must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "must be greater than 0")
        BigDecimal amount
) {}
