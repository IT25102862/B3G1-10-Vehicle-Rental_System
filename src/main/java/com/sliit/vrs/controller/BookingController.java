package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.service.PaymentService;
import com.sliit.vrs.service.ReservationService;
import com.sliit.vrs.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// ===================================================================
// MEMBER 2 - Booking & Reservation Management (customer-facing flow)
// This is the real-world "rent a car" journey:
//   /catalog (public)  --Book Now-->
//   /book/{vehicleId}  (pick dates, needs login)  --submit-->
//   /book/confirmation/{reservationId}  --Proceed to Payment-->
//   /book/payment/{reservationId}  --Pay Now-->
//   /book/receipt/{reservationId}   (booking is CONFIRMED at this point)
// ===================================================================
@Controller
@RequestMapping("/book")
public class BookingController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private PaymentService paymentService;

    // Step 1: show the booking form for a specific vehicle
    @GetMapping("/{vehicleId}")
    public String showBookingForm(@PathVariable Long vehicleId, Model model) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
        Reservation reservation = new Reservation();
        reservation.setVehicle(vehicle);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("reservation", reservation);
        return "booking/book-form";
    }

    // Step 2: submit dates -> validate availability -> create PENDING_APPROVAL booking
    @PostMapping("/{vehicleId}")
    public String submitBooking(@PathVariable Long vehicleId,
                                 @ModelAttribute Reservation reservation,
                                 HttpSession session,
                                 Model model) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
        reservation.setVehicle(vehicle);
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        try {
            Reservation saved = reservationService.createReservation(reservation, loggedInUser);
            return "redirect:/book/confirmation/" + saved.getReservationId();
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("reservation", reservation);
            return "booking/book-form";
        }
    }

    // Step 3: booking summary before payment
    @GetMapping("/confirmation/{id}")
    public String showConfirmation(@PathVariable Long id, Model model) {
        model.addAttribute("reservation", reservationService.getReservationById(id));
        return "booking/confirmation";
    }

    // Step 4: payment form
    @GetMapping("/payment/{id}")
    public String showPaymentForm(@PathVariable Long id, Model model) {
        model.addAttribute("reservation", reservationService.getReservationById(id));
        return "booking/payment";
    }

    // Step 5: process payment -> confirm booking -> show receipt
    @PostMapping("/payment/{id}")
    public String processPayment(@PathVariable Long id, @RequestParam String method) {
        Reservation reservation = reservationService.getReservationById(id);
        paymentService.recordPayment(reservation, method);
        // A completed online payment confirms the booking immediately,
        // matching a real-world rental site's checkout flow.
        reservationService.updateStatus(id, Reservation.ReservationStatus.CONFIRMED);
        return "redirect:/book/receipt/" + id;
    }

    // Step 6: final receipt / "booking confirmed" page
    @GetMapping("/receipt/{id}")
    public String showReceipt(@PathVariable Long id, Model model) {
        model.addAttribute("reservation", reservationService.getReservationById(id));
        return "booking/receipt";
    }
}
