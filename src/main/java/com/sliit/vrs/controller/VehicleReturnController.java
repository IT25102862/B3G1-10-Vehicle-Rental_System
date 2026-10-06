package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Damage;
import com.sliit.vrs.entity.DamageAssessment;
import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.VehicleReturn;
import com.sliit.vrs.service.DamageAssessmentService;
import com.sliit.vrs.service.ReservationService;
import com.sliit.vrs.service.VehicleReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// ===================================================================
// MEMBER 4 (IT25101875 - Bathigama P.L.) - Vehicle Handover, Return &
// Damage Assessment
// ===================================================================
@Controller
@RequestMapping("/returns")
public class VehicleReturnController {

    @Autowired
    private VehicleReturnService returnService;

    @Autowired
    private DamageAssessmentService damageAssessmentService;

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public String listReturns(Model model) {
        model.addAttribute("returns", returnService.getAllReturns());
        return "handover/list";
    }

    @GetMapping("/add")
    public String showReturnForm(Model model) {
        model.addAttribute("vehicleReturn", new VehicleReturn());
        addReturnFormData(model);
        return "handover/return-form";
    }

    @PostMapping("/save")
    public String recordReturn(@ModelAttribute VehicleReturn vehicleReturn, Model model) {
        try {
            returnService.recordReturn(vehicleReturn);
        } catch (RuntimeException ex) {
            // Show the error and keep the values the staff member already entered.
            model.addAttribute("error", ex.getMessage());
            addReturnFormData(model);
            return "handover/return-form";
        }
        return "redirect:/returns";
    }

    // Only CONFIRMED bookings (vehicle currently with the customer) can be
    // returned, so cancelled / pending / already-completed ones are not offered.
    private void addReturnFormData(Model model) {
        model.addAttribute("reservations", reservationService.getAllReservations().stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CONFIRMED)
                .toList());
        model.addAttribute("fuelLevels", VehicleReturnService.FUEL_LEVELS);
    }

    @GetMapping("/{id}/assess")
    public String showAssessmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("vehicleReturn", returnService.getById(id));
        model.addAttribute("assessment", new DamageAssessment());
        model.addAttribute("conditions", DamageAssessmentService.CONDITIONS);
        return "handover/assessment-form";
    }

    @PostMapping("/assess/save")
    public String saveAssessment(@ModelAttribute("assessment") DamageAssessment assessment, Model model) {
        try {
            damageAssessmentService.saveAssessment(assessment);
        } catch (RuntimeException ex) {
            if (assessment.getVehicleReturn() == null) {
                throw ex;   // no return to show the form for - let the error page handle it
            }
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("vehicleReturn", assessment.getVehicleReturn());
            model.addAttribute("conditions", DamageAssessmentService.CONDITIONS);
            return "handover/assessment-form";
        }
        return "redirect:/returns";
    }

    @PostMapping("/damage/add")
    public String addDamage(@ModelAttribute Damage damage, RedirectAttributes redirectAttributes) {
        try {
            damageAssessmentService.addDamage(damage);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/returns";
    }
}
