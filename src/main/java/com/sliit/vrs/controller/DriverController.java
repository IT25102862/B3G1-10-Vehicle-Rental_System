package com.sliit.vrs.controller;

import com.sliit.vrs.entity.DriverAssignment;
import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.Role;
import com.sliit.vrs.service.DriverAssignmentService;
import com.sliit.vrs.service.EmployeeService;
import com.sliit.vrs.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// MEMBER 6 - Maintenance Operations Management System (Driver Allocation)
@Controller
@RequestMapping("/drivers")
public class DriverController {

    @Autowired
    private DriverAssignmentService assignmentService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public String listAssignments(Model model) {
        model.addAttribute("assignments", assignmentService.getAllAssignments());
        return "maintenance/driver-list";
    }

    @GetMapping("/add")
    public String showForm(Model model) {
        model.addAttribute("assignment", new DriverAssignment());
        addFormData(model);
        return "maintenance/driver-form";
    }

    @PostMapping("/save")
    public String assignDriver(@ModelAttribute("assignment") DriverAssignment assignment, Model model) {
        try {
            assignmentService.assignDriver(assignment);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            addFormData(model);
            return "maintenance/driver-form";
        }
        return "redirect:/drivers";
    }

    // Only real drivers, and only bookings that are still active
    // (not cancelled or completed), are offered in the dropdowns.
    private void addFormData(Model model) {
        model.addAttribute("drivers", employeeService.getEmployeesByRole(Role.DRIVER));
        model.addAttribute("reservations", reservationService.getAllReservations().stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.PENDING_APPROVAL
                        || r.getStatus() == Reservation.ReservationStatus.CONFIRMED)
                .toList());
    }

    @GetMapping("/complete/{id}")
    public String completeAssignment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            assignmentService.updateStatus(id, DriverAssignment.AssignmentStatus.COMPLETED);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/drivers";
    }
}
