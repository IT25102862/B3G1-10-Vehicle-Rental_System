package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    private String name;
    private String phoneNo;
    private String email;
    private String position;       // e.g. "Senior Driver", "Maintenance Technician"

    @Enumerated(EnumType.STRING)
    private Role role;

    // For drivers: current availability
    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus; // nullable for non-drivers

    public enum DriverStatus { AVAILABLE, ON_SHIFT, OFF_DUTY }
}
