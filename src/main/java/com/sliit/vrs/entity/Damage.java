package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// MEMBER 4 - Vehicle Handover, Return & Damage Assessment
@Entity
@Table(name = "damages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Damage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long damageId;

    @ManyToOne
    @JoinColumn(name = "assessment_id", nullable = false)
    private DamageAssessment assessment;

    private String damageType;     // Scratch, Dent, Broken part, etc.
    private String severity;       // Minor, Moderate, Severe
    private Double repairCost;

    @Enumerated(EnumType.STRING)
    private DamageStatus damageStatus;

    public enum DamageStatus { REPORTED, UNDER_REPAIR, RESOLVED }
}
