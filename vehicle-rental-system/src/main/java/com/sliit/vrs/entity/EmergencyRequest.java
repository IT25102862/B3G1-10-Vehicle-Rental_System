package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// ===================================================================
// MEMBER 3 (IT25103726 - Kithushan M.) - Emergency & Roadside Assistance
// ===================================================================
@Entity
@Table(name = "emergency_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emergencyRequestId;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    private EmergencyType emergencyType;

    private String description;
    private String location;
    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private String resolution;

    @ManyToOne
    @JoinColumn(name = "assigned_technician_id")
    private Employee assignedTechnician;

    public enum EmergencyType { BREAKDOWN, ACCIDENT, FLAT_TYRE, FUEL_ISSUE, OTHER }
    public enum RequestStatus { OPEN, TECHNICIAN_ASSIGNED, IN_PROGRESS, RESOLVED, CANCELLED }
}
