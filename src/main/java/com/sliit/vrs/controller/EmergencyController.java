package com.sliit.vrs.controller;

import com.sliit.vrs.entity.EmergencyRequest;
import com.sliit.vrs.entity.Role;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.EmergencyService;
import com.sliit.vrs.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// ===================================================================
// MEMBER 3 (IT25103726 - Kithushan M.) - Emergency & Roadside Assistance
// ===================================================================
@Controller
@RequestMapping("/emergencies")
public class EmergencyController {

    @Autowired
    private EmergencyService emergencyService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public String listRequests(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (isCustomer(loggedInUser)) {
            // Customers only see their own requests (other customers' locations are private).
            model.addAttribute("requests", emergencyService.getAllRequests().stream()
                    .filter(r -> r.getCustomer() != null
                            && r.getCustomer().getUserId().equals(loggedInUser.getUserId()))
                    .toList());
        } else {
            model.addAttribute("requests", emergencyService.getAllRequests());
        }
        return "emergency/list";
    }

    @GetMapping("/add")
    public String showForm(Model model) {
        model.addAttribute("request", new EmergencyRequest());
        model.addAttribute("types", EmergencyRequest.EmergencyType.values());
        return "emergency/form";
    }

    @PostMapping("/save")
    public String createRequest(@ModelAttribute("request") EmergencyRequest request, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        request.setCustomer(loggedInUser);
        try {
            emergencyService.createRequest(request);
        } catch (RuntimeException ex) {
            // Show the error and keep what the user already typed.
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("request", request);
            model.addAttribute("types", EmergencyRequest.EmergencyType.values());
            return "emergency/form";
        }
        return "redirect:/emergencies";
    }

    // Staff only - assigning a technician is not a customer action.
    @GetMapping("/assign/{id}/{technicianId}")
    public String assignTechnician(@PathVariable Long id, @PathVariable Long technicianId,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            requireStaff(session);
            emergencyService.assignTechnician(id, employeeService.getById(technicianId));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/emergencies";
    }

    // Staff only - a customer should not be able to mark their own request as resolved.
    @GetMapping("/resolve/{id}")
    public String resolveRequest(@PathVariable Long id, @RequestParam(required = false) String resolution,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            requireStaff(session);
            emergencyService.updateStatus(id, EmergencyRequest.RequestStatus.RESOLVED, resolution);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/emergencies";
    }

    // Staff can cancel any request; a customer can only cancel their own.
    @GetMapping("/cancel/{id}")
    public String cancelRequest(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            EmergencyRequest request = emergencyService.getById(id);
            if (isCustomer(loggedInUser) && (request.getCustomer() == null
                    || !request.getCustomer().getUserId().equals(loggedInUser.getUserId()))) {
                throw new IllegalArgumentException("You can only cancel your own emergency requests.");
            }
            emergencyService.cancelRequest(id);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/emergencies";
    }

    private boolean isCustomer(User user) {
        return user != null && user.getRole() == Role.CUSTOMER;
    }

    private void requireStaff(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || isCustomer(loggedInUser)) {
            throw new IllegalArgumentException("Only staff members can do this.");
        }
    }
}
