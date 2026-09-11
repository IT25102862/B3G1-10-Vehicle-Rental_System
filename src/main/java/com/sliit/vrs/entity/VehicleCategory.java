package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// ===================================================================
// MEMBER 1 (IT25101915 - Panapitiya P.K.S.C.) - Vehicle Fleet Management
// ===================================================================
@Entity
@Table(name = "vehicle_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VehicleCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String categoryName;   // e.g. Sedan, SUV, Van

    private String description;
    private Integer seatingCapacity;

    @Column(nullable = false)
    private Double baseRate;       // daily rental rate for this category
}
