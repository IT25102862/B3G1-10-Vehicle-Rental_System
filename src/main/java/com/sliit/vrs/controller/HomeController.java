package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Role;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.VehicleCategoryService;
import com.sliit.vrs.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleCategoryService categoryService;

    // Public landing page: shows a hero banner + a handful of featured
    // available vehicles, like a real rental company's homepage.
    @GetMapping("/")
    public String home(Model model) {
        var featured = vehicleService.getAvailableVehicles();
        if (featured.size() > 6) {
            featured = featured.subList(0, 6);
        }
        model.addAttribute("featuredVehicles", featured);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index";
    }

    // Routes staff to the admin dashboard and customers to the customer
    // dashboard, from the same "/dashboard" link used across the app.
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() != Role.CUSTOMER) {
            return "redirect:/admin";
        }
        return "customer/dashboard";
    }
}
