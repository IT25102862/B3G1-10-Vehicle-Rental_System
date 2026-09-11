package com.sliit.vrs.controller;

import com.sliit.vrs.entity.TripPlan;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.TripPlanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// ===================================================================
// MEMBER 5 (IT25103718 - Hitigedara S.S.) - Vehicle Recommendations and
// Trip Planning
// ===================================================================
@Controller
@RequestMapping("/tripplans")
public class TripPlanController {

    @Autowired
    private TripPlanService tripPlanService;

    @GetMapping
    public String listTripPlans(Model model) {
        model.addAttribute("tripPlans", tripPlanService.getAllTripPlans());
        return "tripplan/list";
    }

    @GetMapping("/add")
    public String showForm(Model model) {
        model.addAttribute("tripPlan", new TripPlan());
        return "tripplan/form";
    }

    @PostMapping("/save")
    public String createTripPlan(@ModelAttribute TripPlan tripPlan, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        tripPlan.setCustomer(loggedInUser);
        tripPlanService.createTripPlan(tripPlan);
        return "redirect:/tripplans";
    }

    @GetMapping("/{id}/recommendations")
    public String showRecommendations(@PathVariable Long id, Model model) {
        TripPlan tripPlan = tripPlanService.getById(id);
        model.addAttribute("tripPlan", tripPlan);
        model.addAttribute("recommendations", tripPlanService.generateRecommendations(tripPlan));
        return "tripplan/recommendations";
    }
}
