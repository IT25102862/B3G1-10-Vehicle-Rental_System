package com.sliit.vrs.controller;

import com.sliit.vrs.entity.MaintenanceRecord;
import com.sliit.vrs.entity.Role;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.service.EmployeeService;
import com.sliit.vrs.service.MaintenanceService;
import com.sliit.vrs.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// ===================================================================
// MEMBER 6 (IT25100976 - Malagahamuduna R.P.D.S.) - Maintenance
// Operations Management System
// ===================================================================
@Controller
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public String listRecords(Model model) {
        model.addAttribute("records", maintenanceService.getAllRecords());
        return "maintenance/list";
    }

    @GetMapping("/add")
    public String showForm(Model model) {
        model.addAttribute("record", new MaintenanceRecord());
        addFormData(model);
        return "maintenance/form";
    }

    @PostMapping("/save")
    public String scheduleMaintenance(@ModelAttribute("record") MaintenanceRecord record, Model model) {
        try {
            maintenanceService.scheduleMaintenance(record);
        } catch (RuntimeException ex) {
            // Show the error and keep what the user already entered.
            model.addAttribute("error", ex.getMessage());
            addFormData(model);
            return "maintenance/form";
        }
        return "redirect:/maintenance";
    }

    // Rented vehicles cannot go into maintenance, and only maintenance staff
    // can be chosen as the technician.
    private void addFormData(Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles().stream()
                .filter(v -> v.getAvailabilityStatus() != Vehicle.AvailabilityStatus.RENTED)
                .toList());
        model.addAttribute("technicians", employeeService.getEmployeesByRole(Role.MAINTENANCE_STAFF));
    }

    @GetMapping("/complete/{id}")
    public String completeMaintenance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return changeStatus(id, MaintenanceRecord.MaintenanceStatus.COMPLETED, redirectAttributes);
    }

    @GetMapping("/inservice/{id}")
    public String setInService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return changeStatus(id, MaintenanceRecord.MaintenanceStatus.IN_SERVICE, redirectAttributes);
    }

    @GetMapping("/cancel/{id}")
    public String cancelMaintenance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            maintenanceService.cancelMaintenance(id);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/maintenance";
    }

    private String changeStatus(Long id, MaintenanceRecord.MaintenanceStatus status, RedirectAttributes redirectAttributes) {
        try {
            maintenanceService.updateStatus(id, status);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/maintenance";
    }
}
