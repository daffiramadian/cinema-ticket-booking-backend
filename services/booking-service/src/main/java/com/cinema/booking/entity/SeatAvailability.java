package com.cinema.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "seat_availabilities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"showtimeId", "seatNumber"})
)
public class SeatAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String showtimeId;

    @Column(nullable = false, length = 10)
    private String seatNumber;

    @Column(nullable = false, length = 2)
    private String rowLabel;

    @Column(nullable = false)
    private Integer seatCol;

    @Column(nullable = false, length = 20)
    private String status;

    private String bookingId;

    private OffsetDateTime heldUntil;

    public SeatAvailability() {}

    public SeatAvailability(String showtimeId, String seatNumber, String rowLabel, Integer seatCol, String status) {
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.rowLabel = rowLabel;
        this.seatCol = seatCol;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public Integer getSeatCol() { return seatCol; }
    public void setSeatCol(Integer seatCol) { this.seatCol = seatCol; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public OffsetDateTime getHeldUntil() { return heldUntil; }
    public void setHeldUntil(OffsetDateTime heldUntil) { this.heldUntil = heldUntil; }
}
