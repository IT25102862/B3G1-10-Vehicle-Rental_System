package com.sliit.vrs.controller;

import com.sliit.vrs.entity.MaintenanceRecord;
import com.sliit.vrs.service.EmployeeService;
import com.sliit.vrs.service.MaintenanceService;
import com.sliit.vrs.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("technicians", employeeService.getAllEmployees());
        return "maintenance/form";
    }

    @PostMapping("/save")
    public String scheduleMaintenance(@ModelAttribute MaintenanceRecord record) {
        maintenanceService.scheduleMaintenance(record);
        return "redirect:/maintenance";
    }

    @GetMapping("/complete/{id}")
    public String completeMaintenance(@PathVariable Long id) {
        maintenanceService.updateStatus(id, MaintenanceRecord.MaintenanceStatus.COMPLETED);
        return "redirect:/maintenance";
    }

    @GetMapping("/inservice/{id}")
    public String setInService(@PathVariable Long id) {
        maintenanceService.updateStatus(id, MaintenanceRecord.MaintenanceStatus.IN_SERVICE);
        return "redirect:/maintenance";
    }

    @GetMapping("/cancel/{id}")
    public String cancelMaintenance(@PathVariable Long id) {
        maintenanceService.cancelMaintenance(id);
        return "redirect:/maintenance";
    }
}
