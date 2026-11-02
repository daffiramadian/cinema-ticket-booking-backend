package com.cinema.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class BookingActionInput {
    @NotBlank(message = "Action is required")
    private String action;

    public BookingActionInput() {}

    public BookingActionInput(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
