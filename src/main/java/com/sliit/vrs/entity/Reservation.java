package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

// ===================================================================
// MEMBER 2 (IT25102862 - Sandaruwan K.G.A.) - Booking & Reservation
// Management (includes Payment, since Payment & Billing was not
// assigned its own dedicated member in the proposal's "6 Major
// Functions" section - see ASSUMPTIONS in README).
// ===================================================================
@Entity
@Table(name = "reservations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private LocalDate reservationDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String pickupLocation;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Double totalAmount;

    public enum ReservationStatus { PENDING_APPROVAL, CONFIRMED, CANCELLED, COMPLETED }
}
