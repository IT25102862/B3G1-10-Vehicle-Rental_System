package com.sliit.vrs.service;

import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    // ----- Vehicle input rules -----
    private static final int MIN_YEAR = 1980;
    private static final double MAX_MILEAGE = 2_000_000;
    private static final int MAX_SEATS = 60;
    private static final double MAX_FUEL_CONSUMPTION = 100;   // km per litre (or per kWh)

    /**
     * Server-side checks for the add/edit vehicle form. Throws
     * IllegalArgumentException with a message the form can display.
     * Also trims text and stores the registration number in upper case so
     * "wp-cab-1234" and "WP-CAB-1234" are treated as the same vehicle.
     */
    public void validateVehicle(Vehicle vehicle) {
        if (vehicle.getRegistrationNo() == null || vehicle.getRegistrationNo().isBlank()) {
            throw new IllegalArgumentException("Registration number is required.");
        }
        String regNo = vehicle.getRegistrationNo().trim().toUpperCase();
        if (!regNo.matches("[A-Z0-9 -]{4,15}") || !regNo.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Registration number must be 4-15 characters (letters, numbers, spaces or '-') and contain at least one number, e.g. WP-CAB-1234.");
        }
        vehicle.setRegistrationNo(regNo);

        boolean duplicate = (vehicle.getVehicleId() == null)
                ? vehicleRepository.existsByRegistrationNoIgnoreCase(regNo)
                : vehicleRepository.existsByRegistrationNoIgnoreCaseAndVehicleIdNot(regNo, vehicle.getVehicleId());
        if (duplicate) {
            throw new IllegalArgumentException("A vehicle with registration number " + regNo + " already exists.");
        }

        requireText(vehicle.getBrand(), "Brand", 50);
        requireText(vehicle.getModel(), "Model", 50);
        vehicle.setBrand(vehicle.getBrand().trim());
        vehicle.setModel(vehicle.getModel().trim());

        if (vehicle.getCategory() == null) {
            throw new IllegalArgumentException("Please select a vehicle category.");
        }

        int maxYear = LocalDate.now().getYear() + 1;
        if (vehicle.getYear() != null && (vehicle.getYear() < MIN_YEAR || vehicle.getYear() > maxYear)) {
            throw new IllegalArgumentException("Year must be between " + MIN_YEAR + " and " + maxYear + ".");
        }
        if (vehicle.getMileage() != null && (vehicle.getMileage() < 0 || vehicle.getMileage() > MAX_MILEAGE)) {
            throw new IllegalArgumentException("Mileage cannot be negative (maximum " + (long) MAX_MILEAGE + " km).");
        }
        if (vehicle.getSeats() != null && (vehicle.getSeats() < 1 || vehicle.getSeats() > MAX_SEATS)) {
            throw new IllegalArgumentException("Seats must be between 1 and " + MAX_SEATS + ".");
        }
        // Fuel consumption is divided into the trip distance in TripPlanService,
        // so zero or a negative number would break the fuel cost estimate.
        if (vehicle.getFuelConsumption() != null
                && (vehicle.getFuelConsumption() <= 0 || vehicle.getFuelConsumption() > MAX_FUEL_CONSUMPTION)) {
            throw new IllegalArgumentException("Fuel consumption must be greater than 0 and at most " + (int) MAX_FUEL_CONSUMPTION + " km per litre.");
        }
        if (vehicle.getTransmission() != null && !vehicle.getTransmission().isBlank()
                && !vehicle.getTransmission().equals("Automatic") && !vehicle.getTransmission().equals("Manual")) {
            throw new IllegalArgumentException("Transmission must be Automatic or Manual.");
        }
        checkMaxLength(vehicle.getColor(), "Color", 30);
        checkMaxLength(vehicle.getLocation(), "Pickup location", 100);
        checkMaxLength(vehicle.getDescription(), "Description", 1000);
    }

    private void requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        checkMaxLength(value.trim(), fieldName, maxLength);
    }

    private void checkMaxLength(String value, String fieldName, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " is too long (maximum " + maxLength + " characters).");
        }
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



public List<Vehicle> searchCatalog(String keyword, Long categoryId, String transmission,
                                    String fuelType, Integer minSeats, String sortBy) {

    List<Vehicle> results = getAvailableVehicles().stream()
            .filter(v -> keyword == null || keyword.isBlank()
                    || (v.getBrand() != null && v.getBrand().toLowerCase().contains(keyword.toLowerCase()))
                    || (v.getModel() != null && v.getModel().toLowerCase().contains(keyword.toLowerCase())))
            .filter(v -> categoryId == null
                    || (v.getCategory() != null && v.getCategory().getCategoryId().equals(categoryId)))
            .filter(v -> transmission == null || transmission.isBlank()
                    || transmission.equalsIgnoreCase(v.getTransmission())) // transmission එක String නිසා .name() අවශ්‍ය නැත
            .filter(v -> fuelType == null || fuelType.isBlank()
                    || (v.getFuelType() != null && fuelType.equalsIgnoreCase(v.getFuelType().name()))) // fuelType එක Enum නිසා .name() යෙදිය යුතුය
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
