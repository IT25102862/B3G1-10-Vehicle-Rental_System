package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.PaymentService;
import com.sliit.vrs.service.ReservationService;
import com.sliit.vrs.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// ===================================================================
// MEMBER 2 (IT25102862 - Sandaruwan K.G.A.) - Booking & Reservation
// Management
// This controller is the STAFF-SIDE view: see every booking in the
// system and approve/cancel it. For the customer-facing "book a car"
// flow (browse -> pick dates -> pay -> confirmation), see BookingController.
// ===================================================================
@Controller
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PaymentService paymentService;

    // Staff: list every booking in the system (all customers)
    @GetMapping
    public String listReservations(Model model) {
        model.addAttribute("reservations", reservationService.getAllReservations());
        return "reservation/list";
    }

    // Staff: manually create a booking on behalf of a walk-in customer
    @GetMapping("/add")
    public String showBookingForm(Model model) {
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("availableVehicles", vehicleService.getAvailableVehicles());
        return "reservation/form";
    }

    @PostMapping("/save")
    public String createReservation(@ModelAttribute Reservation reservation,
                                     HttpSession session,
                                     Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        try {
            reservationService.createReservation(reservation, loggedInUser);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("availableVehicles", vehicleService.getAvailableVehicles());
            return "reservation/form";
        }
        return "redirect:/reservations";
    }

    // Operations Supervisor approves a pending booking
    @GetMapping("/confirm/{id}")
    public String confirmReservation(@PathVariable Long id) {
        reservationService.updateStatus(id, Reservation.ReservationStatus.CONFIRMED);
        return "redirect:/reservations";
    }

    @GetMapping("/cancel/{id}")
    public String cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return "redirect:/reservations";
    }

    @GetMapping("/complete/{id}")
    public String completeReservation(@PathVariable Long id) {
        reservationService.updateStatus(id, Reservation.ReservationStatus.COMPLETED);
        return "redirect:/reservations";
    }

    // Record a payment for a reservation (staff recording a cash/offline payment)
    @PostMapping("/pay/{id}")
    public String recordPayment(@PathVariable Long id, @RequestParam String method) {
        Reservation reservation = reservationService.getReservationById(id);
        paymentService.recordPayment(reservation, method);
        return "redirect:/reservations";
    }
}
