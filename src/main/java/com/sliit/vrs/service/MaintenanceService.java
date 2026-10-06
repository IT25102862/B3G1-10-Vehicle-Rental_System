package com.sliit.vrs.service;

import com.sliit.vrs.entity.MaintenanceRecord;
import com.sliit.vrs.entity.Role;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.repository.MaintenanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// ===================================================================
// MEMBER 6 (IT25100976 - Malagahamuduna R.P.D.S.) - Maintenance
// Operations Management System
// ===================================================================
@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRecordRepository maintenanceRepository;

    @Autowired
    private VehicleService vehicleService;

    public List<MaintenanceRecord> getAllRecords() {
        return maintenanceRepository.findAll();
    }

    public MaintenanceRecord getById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found with id: " + id));
    }

    private static final List<MaintenanceRecord.MaintenanceStatus> OPEN_STATUSES =
            List.of(MaintenanceRecord.MaintenanceStatus.SCHEDULED, MaintenanceRecord.MaintenanceStatus.IN_SERVICE);
    private static final int MAX_DAYS_AHEAD = 365;
    private static final double MAX_COST = 10_000_000;   // Rs. - sanity limit

    // Server-side checks for the "Schedule Maintenance" form.
    public void validateMaintenance(MaintenanceRecord record) {
        Vehicle vehicle = record.getVehicle();
        if (vehicle == null) {
            throw new IllegalArgumentException("Please select a valid vehicle.");
        }
        // Putting a rented vehicle into maintenance would break the customer's
        // active rental (and completing it later would wrongly mark it AVAILABLE).
        if (vehicle.getAvailabilityStatus() == Vehicle.AvailabilityStatus.RENTED) {
            throw new IllegalStateException("Vehicle " + vehicle.getRegistrationNo()
                    + " is currently rented out. Schedule maintenance after it has been returned.");
        }
        if (maintenanceRepository.existsByVehicleAndStatusIn(vehicle, OPEN_STATUSES)) {
            throw new IllegalStateException("Vehicle " + vehicle.getRegistrationNo()
                    + " already has an open maintenance job. Complete or cancel it first.");
        }
        if (record.getMaintenanceType() == null || record.getMaintenanceType().isBlank()) {
            throw new IllegalArgumentException("Maintenance type is required.");
        }
        record.setMaintenanceType(record.getMaintenanceType().trim());
        if (record.getMaintenanceType().length() > 100) {
            throw new IllegalArgumentException("Maintenance type is too long (maximum 100 characters).");
        }
        if (record.getDescription() != null && record.getDescription().length() > 255) {
            throw new IllegalArgumentException("Description is too long (maximum 255 characters).");
        }

        LocalDate today = LocalDate.now();
        if (record.getScheduledDate() == null) {
            throw new IllegalArgumentException("Scheduled date is required.");
        }
        if (record.getScheduledDate().isBefore(today)) {
            throw new IllegalArgumentException("Scheduled date cannot be before today.");
        }
        if (record.getScheduledDate().isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new IllegalArgumentException("Maintenance can only be scheduled up to " + MAX_DAYS_AHEAD + " days ahead.");
        }
        // Zero is allowed (e.g. free warranty service), negative is not.
        if (record.getCost() != null && (record.getCost() < 0 || record.getCost() > MAX_COST)) {
            throw new IllegalArgumentException("Estimated cost must be between 0 and " + (long) MAX_COST + ".");
        }
        if (record.getTechnician() != null && record.getTechnician().getRole() != Role.MAINTENANCE_STAFF) {
            throw new IllegalArgumentException("The selected technician is not a maintenance staff member.");
        }
    }

    public MaintenanceRecord scheduleMaintenance(MaintenanceRecord record) {
        record.setMaintenanceId(null);   // this form always creates a new record
        validateMaintenance(record);
        record.setStatus(MaintenanceRecord.MaintenanceStatus.SCHEDULED);
        MaintenanceRecord saved = maintenanceRepository.save(record);

        // A vehicle scheduled for maintenance should not be rented out.
        Vehicle vehicle = saved.getVehicle();
        vehicle.setAvailabilityStatus(Vehicle.AvailabilityStatus.UNDER_MAINTENANCE);
        vehicleService.saveVehicle(vehicle);
        return saved;
    }

    // Allowed: SCHEDULED -> IN_SERVICE or COMPLETED, IN_SERVICE -> COMPLETED.
    // COMPLETED is final (e.g. typing /maintenance/inservice/{id} on a finished job is rejected).
    public void updateStatus(Long id, MaintenanceRecord.MaintenanceStatus status) {
        MaintenanceRecord record = getById(id);
        MaintenanceRecord.MaintenanceStatus current = record.getStatus();
        boolean allowed =
                (current == MaintenanceRecord.MaintenanceStatus.SCHEDULED
                        && (status == MaintenanceRecord.MaintenanceStatus.IN_SERVICE
                            || status == MaintenanceRecord.MaintenanceStatus.COMPLETED))
                || (current == MaintenanceRecord.MaintenanceStatus.IN_SERVICE
                        && status == MaintenanceRecord.MaintenanceStatus.COMPLETED);
        if (!allowed) {
            throw new IllegalStateException("Maintenance #" + id + " is " + current + " and cannot be changed to " + status + ".");
        }
        record.setStatus(status);
        maintenanceRepository.save(record);

        if (status == MaintenanceRecord.MaintenanceStatus.COMPLETED) {
            releaseVehicle(record.getVehicle());
        }
    }

    public void cancelMaintenance(Long id) {
        MaintenanceRecord record = getById(id);
        // Completed jobs are maintenance history and must not be deleted.
        if (record.getStatus() == MaintenanceRecord.MaintenanceStatus.COMPLETED) {
            throw new IllegalStateException("Maintenance #" + id + " is already COMPLETED and cannot be cancelled.");
        }
        maintenanceRepository.delete(record);
        releaseVehicle(record.getVehicle());
    }

    // Only put the vehicle back to AVAILABLE if it is still marked as under
    // maintenance - never overwrite a RENTED status.
    private void releaseVehicle(Vehicle vehicle) {
        if (vehicle != null && vehicle.getAvailabilityStatus() == Vehicle.AvailabilityStatus.UNDER_MAINTENANCE) {
            vehicleService.markAsAvailable(vehicle.getVehicleId());
        }
    }
}
