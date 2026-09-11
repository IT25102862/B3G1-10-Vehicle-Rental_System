package com.sliit.vrs.service;

import com.sliit.vrs.entity.FuelPrice;
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
    public FuelPrice saveFuelPrice(FuelPrice fuelPrice) {
        return fuelPriceRepository.save(fuelPrice);
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

        existingFuelPrice.setFuelType(fuelPrice.getFuelType());
        existingFuelPrice.setPrice(fuelPrice.getPrice());

        return fuelPriceRepository.save(existingFuelPrice);
    }

    // Delete
    public void deleteFuelPrice(Long id) {
        fuelPriceRepository.deleteById(id);
    }
}
