package com.sliit.vrs.controller;

import com.sliit.vrs.entity.EmergencyRequest;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.EmergencyService;
import com.sliit.vrs.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String listRequests(Model model) {
        model.addAttribute("requests", emergencyService.getAllRequests());
        return "emergency/list";
    }

    @GetMapping("/add")
    public String showForm(Model model) {
        model.addAttribute("request", new EmergencyRequest());
        model.addAttribute("types", EmergencyRequest.EmergencyType.values());
        return "emergency/form";
    }

    @PostMapping("/save")
    public String createRequest(@ModelAttribute EmergencyRequest request, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        request.setCustomer(loggedInUser);
        emergencyService.createRequest(request);
        return "redirect:/emergencies";
    }

    @GetMapping("/assign/{id}/{technicianId}")
    public String assignTechnician(@PathVariable Long id, @PathVariable Long technicianId) {
        emergencyService.assignTechnician(id, employeeService.getById(technicianId));
        return "redirect:/emergencies";
    }

    @GetMapping("/resolve/{id}")
    public String resolveRequest(@PathVariable Long id, @RequestParam String resolution) {
        emergencyService.updateStatus(id, EmergencyRequest.RequestStatus.RESOLVED, resolution);
        return "redirect:/emergencies";
    }

    @GetMapping("/cancel/{id}")
    public String cancelRequest(@PathVariable Long id) {
        emergencyService.cancelRequest(id);
        return "redirect:/emergencies";
    }
}
