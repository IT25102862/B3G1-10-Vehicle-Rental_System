package com.sliit.vrs.controller;

import com.sliit.vrs.entity.FuelPrice;
import com.sliit.vrs.service.FuelPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/fuel")
public class FuelPriceController {

    @Autowired
    private FuelPriceService fuelPriceService;

    // Get all fuel prices
    @GetMapping
    public String getAllFuelPrices(Model model) {
        model.addAttribute("fuelPrices", fuelPriceService.getAllFuelPrices());
        return "fuel/fuel-prices";
    }

    // Show create form
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("fuelPrice", new FuelPrice());
        return "fuel/fuel-form";
    }

    // Create
    @PostMapping("/save")
    public String saveFuelPrice(@ModelAttribute FuelPrice fuelPrice) {
        fuelPriceService.saveFuelPrice(fuelPrice);
        return "redirect:/fuel";
    }

    // Get one by ID
    @GetMapping("/{id}")
    public String getFuelPriceById(@PathVariable Long id, Model model) {

        FuelPrice fuelPrice = fuelPriceService.getFuelPriceById(id)
                .orElseThrow(() -> new RuntimeException("Fuel price not found"));

        model.addAttribute("fuelPrice", fuelPrice);

        return "fuel/fuel-details";
    }

    // Show update form
    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {

        FuelPrice fuelPrice = fuelPriceService.getFuelPriceById(id)
                .orElseThrow(() -> new RuntimeException("Fuel price not found"));

        model.addAttribute("fuelPrice", fuelPrice);

        return "fuel/fuel-form";
    }

    // Update
    @PostMapping("/update/{id}")
    public String updateFuelPrice(
            @PathVariable Long id,
            @ModelAttribute FuelPrice fuelPrice) {

        fuelPriceService.updateFuelPrice(id, fuelPrice);

        return "redirect:/fuel";
    }

    // Delete
    @GetMapping("/delete/{id}")
    public String deleteFuelPrice(@PathVariable Long id) {

        fuelPriceService.deleteFuelPrice(id);

        return "redirect:/fuel";
    }
}