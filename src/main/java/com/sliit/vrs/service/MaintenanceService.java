package com.sliit.vrs.service;

import com.sliit.vrs.entity.MaintenanceRecord;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.repository.MaintenanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public MaintenanceRecord scheduleMaintenance(MaintenanceRecord record) {
        record.setStatus(MaintenanceRecord.MaintenanceStatus.SCHEDULED);
        MaintenanceRecord saved = maintenanceRepository.save(record);
        // A vehicle scheduled for maintenance should not be rented out.
        Vehicle vehicle = saved.getVehicle();
        vehicle.setAvailabilityStatus(Vehicle.AvailabilityStatus.UNDER_MAINTENANCE);
        vehicleService.saveVehicle(vehicle);
        return saved;
    }

    public void updateStatus(Long id, MaintenanceRecord.MaintenanceStatus status) {
        MaintenanceRecord record = getById(id);
        record.setStatus(status);
        maintenanceRepository.save(record);

        if (status == MaintenanceRecord.MaintenanceStatus.COMPLETED) {
            vehicleService.markAsAvailable(record.getVehicle().getVehicleId());
        }
    }

    public void cancelMaintenance(Long id) {
        MaintenanceRecord record = getById(id);
        maintenanceRepository.delete(record);
        vehicleService.markAsAvailable(record.getVehicle().getVehicleId());
    }
}
