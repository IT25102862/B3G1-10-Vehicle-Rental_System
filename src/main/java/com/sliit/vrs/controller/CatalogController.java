package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.service.ReservationService;
import com.sliit.vrs.service.VehicleCategoryService;
import com.sliit.vrs.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

// Public-facing catalog: anyone (even a visitor who hasn't logged in) can
// browse the fleet, search/filter/sort, and view vehicle details. Booking
// itself (see BookingController) still requires login, matching a typical
// commercial rental website (browse freely, sign in only to check out).
@Controller
public class CatalogController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleCategoryService categoryService;

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/catalog")
    public String browseCatalog(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) String transmission,
                                 @RequestParam(required = false) String fuelType,
                                 @RequestParam(required = false) Integer minSeats,
                                 @RequestParam(required = false) String sortBy,
                                 Model model) {

        model.addAttribute("vehicles",
                vehicleService.searchCatalog(keyword, categoryId, transmission, fuelType, minSeats, sortBy));
        model.addAttribute("categories", categoryService.getAllCategories());

        // Echo the filters back so the form stays filled in after search
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("transmission", transmission);
        model.addAttribute("fuelType", fuelType);
        model.addAttribute("minSeats", minSeats);
        model.addAttribute("sortBy", sortBy);
        return "catalog/browse";
    }

    @GetMapping("/catalog/{id}")
    public String vehicleDetail(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleService.getVehicleById(id);
        model.addAttribute("vehicle", vehicle);
        return "catalog/detail";
    }

    // Small JSON endpoint used by the date pickers on the detail page to
    // check availability before the customer commits to the booking form.
    @GetMapping("/catalog/{id}/availability")
    @ResponseBody
    public Map<String, Object> checkAvailability(@PathVariable Long id,
                                                   @RequestParam String start,
                                                   @RequestParam String end) {
        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            if (!endDate.isAfter(startDate)) {
                return Map.of("available", false, "message", "Return date must be after pickup date.");
            }
            boolean available = reservationService.isVehicleAvailable(id, startDate, endDate);
            return Map.of("available", available,
                    "message", available ? "Available for the selected dates!" : "Not available for these dates.");
        } catch (DateTimeParseException e) {
            return Map.of("available", false, "message", "Please choose valid dates.");
        }
    }
}
