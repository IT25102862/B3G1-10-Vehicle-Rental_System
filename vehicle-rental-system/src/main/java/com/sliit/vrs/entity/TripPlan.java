package com.sliit.vrs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

// ===================================================================
// MEMBER 5 (IT25103718 - Hitigedara S.S.) - Vehicle Recommendations
// and Trip Planning
// ===================================================================
@Entity
@Table(name = "trip_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TripPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tripPlanId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    private String tripName;
    private String startLocation;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer passengerCount;
    private Double distanceKm;
    private Double estimatedFuelCost;
}
