package com.cinema.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    private String id;
    private String showtimeId;
    private String seatNumber;
    private String status;
    private OffsetDateTime expiresAt;

    public Booking() {}

    public Booking(String id, String showtimeId, String seatNumber, String status, OffsetDateTime expiresAt) {
        this.id = id;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
}
