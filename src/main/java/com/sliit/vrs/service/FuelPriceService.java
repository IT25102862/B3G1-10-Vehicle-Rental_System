package com.sliit.vrs.service;

import com.sliit.vrs.entity.FuelPrice;
import com.sliit.vrs.entity.FuelType;
import com.sliit.vrs.repository.FuelPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FuelPriceService {
    @Autowired
    private FuelPriceRepository fuelPriceRepository;

    // Create
    private static final double MAX_PRICE = 10_000;   // Rs. per litre / kWh - sanity limit

    // A fuel price must have a type and a positive price. Zero or a negative
    // price would make every trip's fuel cost estimate wrong.
    public void validateFuelPrice(FuelPrice fuelPrice) {
        if (fuelPrice.getFuelType() == null) {
            throw new IllegalArgumentException("Please select a fuel type.");
        }
        if (fuelPrice.getPrice() <= 0 || fuelPrice.getPrice() > MAX_PRICE) {
            throw new IllegalArgumentException("Fuel price must be greater than 0 and at most Rs. " + (int) MAX_PRICE + ".");
        }
    }

    public FuelPrice saveFuelPrice(FuelPrice fuelPrice) {
        validateFuelPrice(fuelPrice);
        if (existFuelPriceByType(fuelPrice.getFuelType())) {
            throw new RuntimeException("Fuel Type Already Exist");
        }
        return fuelPriceRepository.save(fuelPrice);
    }

    public boolean existFuelPriceByType(FuelType fuelType) {
        return fuelPriceRepository.existsFuelPriceByFuelType(fuelType);
    }

    // Get all
    public List<FuelPrice> getAllFuelPrices() {
        return fuelPriceRepository.findAll();
    }

    // Get by ID
    public Optional<FuelPrice> getFuelPriceById(Long id) {
        return fuelPriceRepository.findById(id);
    }

    // Update
    public FuelPrice updateFuelPrice(Long id, FuelPrice fuelPrice) {

        FuelPrice existingFuelPrice = fuelPriceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fuel price not found"));
        validateFuelPrice(fuelPrice);

        if (fuelPriceRepository.existsByFuelTypeAndFuelPriceIdNot(
                fuelPrice.getFuelType(), id)) {
            throw new RuntimeException("Fuel Type Already Exist");
        }

        existingFuelPrice.setFuelType(fuelPrice.getFuelType());
        existingFuelPrice.setPrice(fuelPrice.getPrice());

        return fuelPriceRepository.save(existingFuelPrice);
    }

    // Delete
    public void deleteFuelPrice(Long id) {
        fuelPriceRepository.deleteById(id);
    }

    public double getFuelPriceByType(FuelType fuelType) {
        FuelPrice fuelPrice = fuelPriceRepository.getFuelPriceByFuelType(fuelType);
        if (fuelPrice == null) {
            throw new RuntimeException("No fuel price has been set for " + fuelType + ".");
        }
        return fuelPrice.getPrice();
    }
}
