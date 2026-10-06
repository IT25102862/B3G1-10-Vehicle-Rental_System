package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Role;
import com.sliit.vrs.entity.TripPlan;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.TripPlanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String listTripPlans(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (isCustomer(loggedInUser)) {
            // Customers only see their own trip plans.
            model.addAttribute("tripPlans", tripPlanService.getAllTripPlans().stream()
                    .filter(t -> t.getCustomer() != null
                            && t.getCustomer().getUserId().equals(loggedInUser.getUserId()))
                    .toList());
        } else {
            model.addAttribute("tripPlans", tripPlanService.getAllTripPlans());
        }
        return "tripplan/list";
    }

    @GetMapping("/add")
    public String showForm(Model model) {
        model.addAttribute("tripPlan", new TripPlan());
        return "tripplan/form";
    }

    @GetMapping("/edit/{id}")
    public String editTripPlan(@PathVariable Long id, HttpSession session, Model model) {
        TripPlan tripPlan = getAllowedTripPlan(id, session);
        model.addAttribute("tripPlan", tripPlan);
        return "tripplan/form";
    }

    @PostMapping("/save")
    public String createTripPlan(
            @ModelAttribute TripPlan tripPlan,
            HttpSession session,
            Model model) {

        User loggedInUser =
                (User) session.getAttribute("loggedInUser");

        try {
            if (tripPlan.getTripPlanId() != null) {
                // Editing: keep the original owner instead of re-assigning
                // the trip to whoever pressed "Update".
                TripPlan existing = getAllowedTripPlan(tripPlan.getTripPlanId(), session);
                tripPlan.setCustomer(existing.getCustomer());
            } else {
                tripPlan.setCustomer(loggedInUser);
            }
            tripPlanService.createTripPlan(tripPlan);
        } catch (RuntimeException ex) {
            // Show the error and keep what the user already typed.
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("tripPlan", tripPlan);
            return "tripplan/form";
        }

        return "redirect:/tripplans";
    }

    @GetMapping("/delete/{id}")
    public String deleteTripPlan(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            getAllowedTripPlan(id, session);
            tripPlanService.deleteTripPlan(id);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/tripplans";
    }

    @GetMapping("/{id}/recommendations")
    public String showRecommendations(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            TripPlan tripPlan = getAllowedTripPlan(id, session);
            model.addAttribute("tripPlan", tripPlan);
            model.addAttribute(
                    "recommendations",
                    tripPlanService.generateRecommendations(tripPlan)
            );
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/tripplans";
        }

        return "tripplan/recommendations";
    }

    // Loads a trip plan and makes sure a customer can only open their own
    // (staff can open any). Stops users changing the ID in the URL.
    private TripPlan getAllowedTripPlan(Long id, HttpSession session) {
        TripPlan tripPlan = tripPlanService.getById(id);
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (isCustomer(loggedInUser) && (tripPlan.getCustomer() == null
                || !tripPlan.getCustomer().getUserId().equals(loggedInUser.getUserId()))) {
            throw new IllegalArgumentException("You can only manage your own trip plans.");
        }
        return tripPlan;
    }

    private boolean isCustomer(User user) {
        return user != null && user.getRole() == Role.CUSTOMER;
    }
}
