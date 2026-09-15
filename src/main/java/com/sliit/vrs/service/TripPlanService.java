package com.sliit.vrs.service;

import com.sliit.vrs.entity.TripPlan;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.entity.VehicleRecommendation;
import com.sliit.vrs.repository.TripPlanRepository;
import com.sliit.vrs.repository.VehicleRecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public TripPlan createTripPlan(TripPlan tripPlan) {
        return tripPlanRepository.save(tripPlan);
    }

    // Simple rule-based recommendation: suggest available vehicles whose
    // category seating capacity covers the passenger count, scored by how
    // closely the seating capacity matches (less wasted seats = higher score).
    public List<VehicleRecommendation> generateRecommendations(TripPlan tripPlan) {
        List<Vehicle> available = vehicleService.getAvailableVehicles();
        List<VehicleRecommendation> results = new ArrayList<>();

        for (Vehicle v : available) {
            if (v.getCategory() == null || v.getCategory().getSeatingCapacity() == null) continue;
            int seats = v.getCategory().getSeatingCapacity();
            int passengers = tripPlan.getPassengerCount() == null ? 1 : tripPlan.getPassengerCount();
            if (seats < passengers) continue;

            double score = 100.0 - ((seats - passengers) * 5.0);
            if (score < 0) score = 0;

            double fuelPrice = fuelPriceService.getFuelPriceByType(v.getFuelType());

            double estimatedFuelCost =
                    Math.round(
                            (tripPlan.getDistanceKm() / v.getFuelConsumption()) * fuelPrice * 100.0
                    ) / 100.0;


            VehicleRecommendation rec = new VehicleRecommendation();
            rec.setTripPlan(tripPlan);
            rec.setVehicle(v);
            rec.setSuitabilityScore(score);
            rec.setReason(seats + "-seat " + v.getCategory().getCategoryName() + " fits " + passengers + " passenger(s)");
            rec.setEstimatedFuelCost(estimatedFuelCost);

            results.add(recommendationRepository.save(rec));
        }
        return results;
    }

    public List<VehicleRecommendation> getRecommendationsForTrip(Long tripPlanId) {
        return recommendationRepository.findAll().stream()
                .filter(r -> r.getTripPlan().getTripPlanId().equals(tripPlanId))
                .toList();
    }
}
