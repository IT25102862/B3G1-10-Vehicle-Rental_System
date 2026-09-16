package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

// MEMBER 2 - Booking & Reservation Management (Payment sub-feature)
@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private Double amount;
    private LocalDate paymentDate;
    private String paymentMethod;      // Cash, Card, Bank Transfer (recorded internally - no external gateway)

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String transactionReference;

    public enum PaymentStatus { PENDING, PAID, REFUNDED }
}
