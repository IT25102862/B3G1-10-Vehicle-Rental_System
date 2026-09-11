package com.sliit.vrs.service;

import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// MEMBER 1 - Vehicle Fleet Management
// Business logic lives here, separate from the controller (presentation)
// and the repository (data access) - a simple 3-layer architecture.
@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByAvailabilityStatus(Vehicle.AvailabilityStatus.AVAILABLE);
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        // Create or Update - JPA's save() does both.
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }

    public void markAsRented(Long vehicleId) {
        Vehicle v = getVehicleById(vehicleId);
        v.setAvailabilityStatus(Vehicle.AvailabilityStatus.RENTED);
        vehicleRepository.save(v);
    }

    public void markAsAvailable(Long vehicleId) {
        Vehicle v = getVehicleById(vehicleId);
        v.setAvailabilityStatus(Vehicle.AvailabilityStatus.AVAILABLE);
        vehicleRepository.save(v);
    }
}
