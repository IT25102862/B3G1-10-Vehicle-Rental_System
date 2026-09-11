package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// MEMBER 5 - Vehicle Recommendations and Trip Planning
@Entity
@Table(name = "vehicle_recommendations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VehicleRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendationId;

    @ManyToOne
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    private Double suitabilityScore;   // 0-100, simple rule-based score
    private String reason;
    private Double estimatedFuelCost;
}
