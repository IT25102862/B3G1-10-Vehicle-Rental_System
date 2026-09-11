package com.sliit.vrs.controller;

import com.sliit.vrs.entity.DriverAssignment;
import com.sliit.vrs.service.DriverAssignmentService;
import com.sliit.vrs.service.EmployeeService;
import com.sliit.vrs.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("drivers", employeeService.getAllEmployees());
        model.addAttribute("reservations", reservationService.getAllReservations());
        return "maintenance/driver-form";
    }

    @PostMapping("/save")
    public String assignDriver(@ModelAttribute DriverAssignment assignment) {
        assignmentService.assignDriver(assignment);
        return "redirect:/drivers";
    }

    @GetMapping("/complete/{id}")
    public String completeAssignment(@PathVariable Long id) {
        assignmentService.updateStatus(id, DriverAssignment.AssignmentStatus.COMPLETED);
        return "redirect:/drivers";
    }
}
