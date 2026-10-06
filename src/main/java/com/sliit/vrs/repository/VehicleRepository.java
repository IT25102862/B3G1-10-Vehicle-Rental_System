package com.sliit.vrs.repository;

import com.sliit.vrs.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByAvailabilityStatus(Vehicle.AvailabilityStatus status);

    // Duplicate registration number checks (registrationNo is UNIQUE in the DB).
    boolean existsByRegistrationNoIgnoreCase(String registrationNo);

    boolean existsByRegistrationNoIgnoreCaseAndVehicleIdNot(String registrationNo, Long vehicleId);
}
