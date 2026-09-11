package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.service.VehicleCategoryService;
import com.sliit.vrs.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// ===================================================================
// MEMBER 1 (IT25101915 - Panapitiya P.K.S.C.) - Vehicle Fleet Management
// Handles: Create / Read / Update / Delete vehicles, vehicle categories,
// and vehicle status (Available / Rented / Under Maintenance).
// ===================================================================
@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleCategoryService categoryService;

    // READ - list all vehicles
    @GetMapping
    public String listVehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        return "vehicle/list";
    }

    // CREATE - show empty form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", Vehicle.AvailabilityStatus.values());
        return "vehicle/form";
    }

    // CREATE / UPDATE - save
    @PostMapping("/save")
    public String saveVehicle(@ModelAttribute Vehicle vehicle) {
        vehicleService.saveVehicle(vehicle);
        return "redirect:/vehicles";
    }

    // UPDATE - show pre-filled form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("vehicle", vehicleService.getVehicleById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", Vehicle.AvailabilityStatus.values());
        return "vehicle/form";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return "redirect:/vehicles";
    }
}
