package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

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
    private LocalTime pickupTime;

    private LocalDate endDate;
    private LocalTime returnTime;

    private String pickupLocation;
    private String dropoffLocation;

    @Column(length = 500)
    private String notes;          // customer's special requests

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Double totalAmount;

    public enum ReservationStatus { PENDING_APPROVAL, CONFIRMED, CANCELLED, COMPLETED }
}
