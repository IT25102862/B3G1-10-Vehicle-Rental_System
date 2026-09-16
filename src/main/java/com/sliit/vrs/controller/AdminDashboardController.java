package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Payment;
import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Admin Dashboard: a single overview screen (stats + quick links) so staff
// don't have to hunt through six separate modules to see how the business
// is doing. This is the "Reporting and Administrative Functions" minor
// function described in the proposal.
@Controller
public class AdminDashboardController {

    @Autowired private VehicleService vehicleService;
    @Autowired private ReservationService reservationService;
    @Autowired private UserService userService;
    @Autowired private PaymentService paymentService;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        var vehicles = vehicleService.getAllVehicles();
        var reservations = reservationService.getAllReservations();
        var payments = paymentService.getAllPayments();

        long available = vehicles.stream().filter(v -> v.getAvailabilityStatus() == Vehicle.AvailabilityStatus.AVAILABLE).count();
        long rented = vehicles.stream().filter(v -> v.getAvailabilityStatus() == Vehicle.AvailabilityStatus.RENTED).count();
        long pending = reservations.stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.PENDING_APPROVAL).count();
        double revenue = payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.PAID)
                .mapToDouble(Payment::getAmount).sum();

        model.addAttribute("totalVehicles", vehicles.size());
        model.addAttribute("availableVehicles", available);
        model.addAttribute("rentedVehicles", rented);
        model.addAttribute("totalBookings", reservations.size());
        model.addAttribute("pendingBookings", pending);
        model.addAttribute("totalCustomers", userService.getAllCustomers().size());
        model.addAttribute("totalRevenue", revenue);
        model.addAttribute("recentBookings", reservations.stream()
                .sorted((a, b) -> b.getReservationId().compareTo(a.getReservationId()))
                .limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/admin/customers")
    public String customerManagement(Model model) {
        model.addAttribute("customers", userService.getAllCustomers());
        return "admin/customers";
    }
}
