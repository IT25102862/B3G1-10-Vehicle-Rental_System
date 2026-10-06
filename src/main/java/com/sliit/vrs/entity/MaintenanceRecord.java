package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

// ===================================================================
// MEMBER 6 (IT25100976 - Malagahamuduna R.P.D.S.) - Maintenance
// Operations Management System (Maintenance & Driver Allocation)
// ===================================================================
@Entity
@Table(name = "maintenance_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maintenanceId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private String maintenanceType;    // Oil change, Tire rotation, Brake check...
    private String description;
    private LocalDate scheduledDate;
    private Double cost;
    private String remark;

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Employee technician;

    public enum MaintenanceStatus { SCHEDULED, IN_SERVICE, COMPLETED }
}
