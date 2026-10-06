package com.sliit.vrs.repository;

import com.sliit.vrs.entity.MaintenanceRecord;
import com.sliit.vrs.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Spring Data JPA gives us save(), findAll(), findById(), deleteById() etc.
// for free - no SQL needs to be written for basic CRUD.
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    // Used to stop two open maintenance jobs being created for the same vehicle.
    boolean existsByVehicleAndStatusIn(Vehicle vehicle, List<MaintenanceRecord.MaintenanceStatus> statuses);
}
