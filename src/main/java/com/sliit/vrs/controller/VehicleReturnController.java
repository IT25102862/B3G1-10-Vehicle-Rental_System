package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Damage;
import com.sliit.vrs.entity.DamageAssessment;
import com.sliit.vrs.entity.VehicleReturn;
import com.sliit.vrs.service.DamageAssessmentService;
import com.sliit.vrs.service.ReservationService;
import com.sliit.vrs.service.VehicleReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("reservations", reservationService.getAllReservations());
        return "handover/return-form";
    }

    @PostMapping("/save")
    public String recordReturn(@ModelAttribute VehicleReturn vehicleReturn) {
        returnService.recordReturn(vehicleReturn);
        return "redirect:/returns";
    }

    @GetMapping("/{id}/assess")
    public String showAssessmentForm(@PathVariable Long id, Model model) {
        model.addAttribute("vehicleReturn", returnService.getById(id));
        model.addAttribute("assessment", new DamageAssessment());
        return "handover/assessment-form";
    }

    @PostMapping("/assess/save")
    public String saveAssessment(@ModelAttribute DamageAssessment assessment) {
        damageAssessmentService.saveAssessment(assessment);
        return "redirect:/returns";
    }

    @PostMapping("/damage/add")
    public String addDamage(@ModelAttribute Damage damage) {
        damageAssessmentService.addDamage(damage);
        return "redirect:/returns";
    }
}
