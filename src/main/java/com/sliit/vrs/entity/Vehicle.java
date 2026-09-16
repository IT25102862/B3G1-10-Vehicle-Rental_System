package com.sliit.vrs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// MEMBER 1 - Vehicle Fleet Management
// v2: added image + descriptive/spec fields so the public catalog and
// detail page can show a proper vehicle card instead of a plain table row.
@Entity
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @NotBlank(message = "Registration number is required")
    @Column(nullable = false, unique = true)
    private String registrationNo;

    @NotBlank(message = "Brand is required")
    @Column(nullable = false)
    private String brand;

    @NotBlank(message = "Model is required")
    @Column(nullable = false)
    private String model;

    private Integer year;
    private Double mileage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availabilityStatus;

    private Double fuelConsumption;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private VehicleCategory category;

    // ===== New fields for the professional catalog / detail page =====

    // Web path to the uploaded photo, e.g. "/uploads/vehicles/abc123.jpg".
    // Falls back to a placeholder image in the UI if this is null.
    private String imageUrl;

    @Column(length = 1000)
    private String description;

    private String transmission;   // Automatic / Manual
    private Integer seats;
    private String color;
    private String location;       // pickup branch / city, used for filtering

    public enum AvailabilityStatus { AVAILABLE, RENTED, UNDER_MAINTENANCE }
}
