package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

// MEMBER 4 - Vehicle Handover, Return & Damage Assessment
@Entity
@Table(name = "damage_assessments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DamageAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assessmentId;

    @OneToOne
    @JoinColumn(name = "return_id", nullable = false)
    private VehicleReturn vehicleReturn;

    private LocalDate assessmentDate;
    private String overallCondition;
    private Double estimatedRepairCost;
    private String assessorRemark;
}
