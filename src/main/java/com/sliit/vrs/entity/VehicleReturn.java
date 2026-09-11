package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

// ===================================================================
// MEMBER 4 (IT25101875 - Bathigama P.L.) - Vehicle Handover, Return &
// Damage Assessment
// ===================================================================
@Entity
@Table(name = "vehicle_returns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VehicleReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long returnId;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private LocalDate returnDate;
    private LocalTime returnTime;
    private Double returnMileage;
    private String fuelLevel;
    private String returnCondition;

    @ManyToOne
    @JoinColumn(name = "verified_by")
    private Employee verifiedBy;   // Operations Supervisor / Counter Agent
}
