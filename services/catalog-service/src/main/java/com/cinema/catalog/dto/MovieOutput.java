package com.cinema.catalog.dto;

public record MovieOutput(
    String id,
    String title,
    Integer durationMinutes,
    String description
) {}
