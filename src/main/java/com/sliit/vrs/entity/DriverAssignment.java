package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

// MEMBER 6 - Maintenance Operations Management System (Driver Allocation)
@Entity
@Table(name = "driver_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DriverAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Employee driver;

    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    public enum AssignmentStatus { ASSIGNED, ON_TRIP, COMPLETED, CANCELLED }
}
