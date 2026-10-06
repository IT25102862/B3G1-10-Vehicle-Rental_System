package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.service.FileStorageService;
import com.sliit.vrs.service.VehicleCategoryService;
import com.sliit.vrs.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// ===================================================================
// MEMBER 1 (IT25101915 - Panapitiya P.K.S.C.) - Vehicle Fleet Management
// Handles: Create / Read / Update / Delete vehicles, vehicle categories,
// vehicle status (Available / Rented / Under Maintenance), and (v2)
// vehicle photo upload/management.
// ===================================================================
@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleCategoryService categoryService;

    @Autowired
    private FileStorageService fileStorageService;

    // READ - list all vehicles (staff fleet management view)
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

    // CREATE / UPDATE - save, with optional photo upload
    @PostMapping("/save")
    public String saveVehicle(@Valid @ModelAttribute Vehicle vehicle,
                               BindingResult bindingResult,
                               @RequestParam(required = false) MultipartFile imageFile,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", Vehicle.AvailabilityStatus.values());
            return "vehicle/form";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.store(imageFile, "vehicles");
            vehicle.setImageUrl(imageUrl);
        } else if (vehicle.getVehicleId() != null) {
            // Editing an existing vehicle without choosing a new photo:
            // keep the photo it already had instead of wiping it out.
            Vehicle existing = vehicleService.getVehicleById(vehicle.getVehicleId());
            vehicle.setImageUrl(existing.getImageUrl());
        }

        if (vehicle.getAvailabilityStatus() == null) {
            vehicle.setAvailabilityStatus(Vehicle.AvailabilityStatus.AVAILABLE);
        }

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
