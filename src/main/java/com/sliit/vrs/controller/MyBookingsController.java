package com.sliit.vrs.controller;

import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Customer-facing booking history page ("My Bookings" in the customer
// dashboard). Only shows the logged-in customer's own reservations.
@Controller
public class MyBookingsController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/my-bookings")
    public String myBookings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("bookings", reservationService.getBookingsForCustomer(loggedInUser.getUserId()));
        return "customer/my-bookings";
    }
}
