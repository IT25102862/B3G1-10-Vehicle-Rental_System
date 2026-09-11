package com.sliit.vrs.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "fuel_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class FuelPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fuelPriceId;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    private double price;
}
