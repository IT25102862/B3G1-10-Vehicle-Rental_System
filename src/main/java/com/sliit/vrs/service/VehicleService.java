package com.sliit.vrs.service;

import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// MEMBER 1 - Vehicle Fleet Management
// Business logic lives here, separate from the controller (presentation)
// and the repository (data access) - a simple 3-layer architecture.
//
// v2: added searchCatalog() for the public browsing page. It filters and
// sorts in plain Java (streams) rather than a complex database query -
// the fleet size in a demo/small business system is small enough that this
// stays fast, and it is much easier for a student to read and explain than
// a dynamic JPA Specification/Criteria query.
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

    /**
     * Public catalog search/filter/sort.
     * All parameters are optional (pass null / blank to ignore that filter).
     *
     * @param keyword     matches against brand or model (case-insensitive)
     * @param categoryId  only vehicles in this category
     * @param transmission "Automatic" / "Manual"
     * @param fuelType     "Petrol" / "Diesel" / "Hybrid" / "Electric"
     * @param minSeats     minimum seating capacity required
     * @param sortBy       "price_asc", "price_desc", "year_desc", or default (name)
     */
    public List<Vehicle> searchCatalog(String keyword, Long categoryId, String transmission,
                                        String fuelType, Integer minSeats, String sortBy) {

        List<Vehicle> results = getAvailableVehicles().stream()
                .filter(v -> keyword == null || keyword.isBlank()
                        || v.getBrand().toLowerCase().contains(keyword.toLowerCase())
                        || v.getModel().toLowerCase().contains(keyword.toLowerCase()))
                .filter(v -> categoryId == null
                        || (v.getCategory() != null && v.getCategory().getCategoryId().equals(categoryId)))
                .filter(v -> transmission == null || transmission.isBlank()
                        || transmission.equalsIgnoreCase(v.getTransmission()))
                .filter(v -> fuelType == null || fuelType.isBlank()
                        || fuelType.equalsIgnoreCase(String.valueOf(v.getFuelType())))
                .filter(v -> minSeats == null || (v.getSeats() != null && v.getSeats() >= minSeats))
                .collect(Collectors.toList());

        Comparator<Vehicle> comparator = Comparator.comparing(v -> v.getBrand() + v.getModel());
        if ("price_asc".equals(sortBy)) {
            comparator = Comparator.comparingDouble(this::rateOf);
        } else if ("price_desc".equals(sortBy)) {
            comparator = Comparator.comparingDouble(this::rateOf).reversed();
        } else if ("year_desc".equals(sortBy)) {
            comparator = Comparator.comparing((Vehicle v) -> v.getYear() == null ? 0 : v.getYear()).reversed();
        }
        results.sort(comparator);
        return results;
    }

    private double rateOf(Vehicle v) {
        return v.getCategory() != null && v.getCategory().getBaseRate() != null
                ? v.getCategory().getBaseRate() : 0.0;
    }
}
