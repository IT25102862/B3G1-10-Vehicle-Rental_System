package com.sliit.vrs.service;

import com.sliit.vrs.entity.TripPlan;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.entity.VehicleRecommendation;
import com.sliit.vrs.repository.TripPlanRepository;
import com.sliit.vrs.repository.VehicleRecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// ===================================================================
// MEMBER 5 (IT25103718 - Hitigedara S.S.) - Vehicle Recommendations and
// Trip Planning
// ===================================================================
@Service
public class TripPlanService {

    @Autowired
    private TripPlanRepository tripPlanRepository;

    @Autowired
    private VehicleRecommendationRepository recommendationRepository;

    @Autowired
    private VehicleService vehicleService;
    @Autowired
    private FuelPriceService fuelPriceService;

    public List<TripPlan> getAllTripPlans() {
        return tripPlanRepository.findAll();
    }

    public TripPlan getById(Long id) {
        return tripPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip plan not found with id: " + id));
    }

    // ----- Trip plan input rules -----
    public static final int MAX_TRIP_DAYS = 90;
    public static final int MAX_PASSENGERS = 60;
    public static final double MAX_DISTANCE_KM = 10000;

    // Used for both "Plan a New Trip" and "Edit Trip Plan" (same form + save URL).
    public TripPlan createTripPlan(TripPlan tripPlan) {
        validateTripPlan(tripPlan);
        return tripPlanRepository.save(tripPlan);
    }

    public void validateTripPlan(TripPlan tripPlan) {
        tripPlan.setTripName(requireText(tripPlan.getTripName(), "Trip name", 100));
        tripPlan.setStartLocation(requireText(tripPlan.getStartLocation(), "Start location", 255));
        tripPlan.setDestination(requireText(tripPlan.getDestination(), "Destination", 255));

        if (tripPlan.getStartDate() == null || tripPlan.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }
        // A new trip (or a changed start date) cannot start in the past. When
        // editing an older trip without touching its start date, keep allowing it.
        boolean startDateUnchanged = false;
        if (tripPlan.getTripPlanId() != null) {
            TripPlan existing = getById(tripPlan.getTripPlanId());
            startDateUnchanged = tripPlan.getStartDate().equals(existing.getStartDate());
        }
        if (!startDateUnchanged && tripPlan.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be before today.");
        }
        if (tripPlan.getEndDate().isBefore(tripPlan.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before the start date.");
        }
        if (ChronoUnit.DAYS.between(tripPlan.getStartDate(), tripPlan.getEndDate()) > MAX_TRIP_DAYS) {
            throw new IllegalArgumentException("A trip cannot be longer than " + MAX_TRIP_DAYS + " days.");
        }

        if (tripPlan.getPassengerCount() == null
                || tripPlan.getPassengerCount() < 1 || tripPlan.getPassengerCount() > MAX_PASSENGERS) {
            throw new IllegalArgumentException("Passenger count must be between 1 and " + MAX_PASSENGERS + ".");
        }
        // Distance is used to calculate the fuel cost, so it must be a real positive number.
        if (tripPlan.getDistanceKm() == null
                || tripPlan.getDistanceKm() <= 0 || tripPlan.getDistanceKm() > MAX_DISTANCE_KM) {
            throw new IllegalArgumentException("Distance must be greater than 0 and at most " + (int) MAX_DISTANCE_KM + " km.");
        }
    }

    private String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " is too long (maximum " + maxLength + " characters).");
        }
        return trimmed;
    }

    // Simple rule-based recommendation: suggest available vehicles whose
    // category seating capacity covers the passenger count, scored by how
    // closely the seating capacity matches (less wasted seats = higher score).
    @Transactional
    public List<VehicleRecommendation> generateRecommendations(TripPlan tripPlan) {
        // Older trip plans may have been saved before validation existed.
        if (tripPlan.getDistanceKm() == null || tripPlan.getDistanceKm() <= 0
                || tripPlan.getStartDate() == null || tripPlan.getEndDate() == null) {
            throw new IllegalStateException("This trip plan is missing its distance or dates. Please edit the trip plan first.");
        }

        // Remove the previous results first - otherwise every visit to the
        // recommendations page saved another duplicate set of rows.
        recommendationRepository.deleteByTripPlan(tripPlan);

        List<Vehicle> available = vehicleService.getAvailableVehicles();
        List<VehicleRecommendation> results = new ArrayList<>();

        for (Vehicle v : available) {
            // Skip vehicles with missing or invalid data (fuel consumption is
            // divided into the distance below, so it must be greater than 0).
            if (v.getCategory() == null || v.getCategory().getSeatingCapacity() == null
                    || v.getCategory().getBaseRate() == null
                    || v.getFuelConsumption() == null || v.getFuelConsumption() <= 0) continue;
            int seats = v.getCategory().getSeatingCapacity();
            int passengers = tripPlan.getPassengerCount() == null ? 1 : tripPlan.getPassengerCount();
            if (seats < passengers) continue;

            if (!fuelPriceService.existFuelPriceByType(v.getFuelType())) continue;

            double fuelPrice = fuelPriceService.getFuelPriceByType(v.getFuelType());

            double estimatedFuelCost =
                    Math.round(
                            (tripPlan.getDistanceKm() / v.getFuelConsumption()) * fuelPrice * 100.0
                    ) / 100.0;

            // A same-day trip still needs the vehicle for 1 rental day.
            int numberOfDays = Math.max(1, (int) ChronoUnit.DAYS.between(
                    tripPlan.getStartDate(),
                    tripPlan.getEndDate()
            ));

            double rentalCost = estimatedFuelCost + v.getCategory().getBaseRate() * numberOfDays;

            double score = 100.0 - ((seats - passengers) * 5.0);
            if (score < 0) score = 0;

            VehicleRecommendation rec = new VehicleRecommendation();
            rec.setTripPlan(tripPlan);
            rec.setVehicle(v);
            rec.setSuitabilityScore(score);
            rec.setReason(seats + "-seat " + v.getCategory().getCategoryName() + " fits " + passengers + " passenger(s)");
            rec.setEstimatedFuelCost(estimatedFuelCost);
            rec.setRentalCost(rentalCost);

            results.add(recommendationRepository.save(rec));
        }
        return results;
    }

    public List<VehicleRecommendation> getRecommendationsForTrip(Long tripPlanId) {
        return recommendationRepository.findAll().stream()
                .filter(r -> r.getTripPlan().getTripPlanId().equals(tripPlanId))
                .toList();
    }

    @Transactional
    public void deleteTripPlan(Long id) {

        TripPlan tripPlan = tripPlanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Trip Plan not found"));

        recommendationRepository.deleteByTripPlan(tripPlan);

        tripPlanRepository.delete(tripPlan);
    }
}
