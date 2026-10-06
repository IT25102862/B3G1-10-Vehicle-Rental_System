package com.sliit.vrs.repository;

import com.sliit.vrs.entity.FuelPrice;
import com.sliit.vrs.entity.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuelPriceRepository extends JpaRepository<FuelPrice, Long> {
    FuelPrice getFuelPriceByFuelType(FuelType fuelType);

    boolean existsFuelPriceByFuelType(FuelType fuelType);

    boolean existsByFuelTypeAndFuelPriceIdNot(
            FuelType fuelType,
            Long fuelPriceId
    );
}
