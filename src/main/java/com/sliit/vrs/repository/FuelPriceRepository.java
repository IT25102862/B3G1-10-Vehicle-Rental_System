package com.sliit.vrs.repository;

import com.sliit.vrs.entity.FuelPrice;
import com.sliit.vrs.entity.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuelPriceRepository extends JpaRepository<FuelPrice, Long> {
    boolean existsFuelPriceByFuelType(FuelType fuelType);

    boolean existsByFuelTypeAndFuelPriceIdNot(
            FuelType fuelType,
            Long fuelPriceId
    );

    FuelPrice getFuelPriceByFuelType(FuelType fuelType);
}
