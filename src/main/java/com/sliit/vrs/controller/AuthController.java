package com.sliit.vrs.controller;

import com.sliit.vrs.entity.Role;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// Shared / minor function: handles registration, login and logout for
// every user type (customer or staff). This is intentionally built once
// so all six major-function modules can rely on session.getAttribute("loggedInUser").
//
// NOTE for the viva: in a real commercial deployment, the public /register
// page would only ever create CUSTOMER accounts. Staff accounts (Fleet
// Manager, Operations Supervisor, etc.) would be created by an
// Administrator from a protected "Manage Staff" screen instead. We kept
// the role dropdown open here purely so every group member can create a
// test account for their own module without needing a separate admin seed.
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("roles", Role.values());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam(required = false) String confirmPassword,
                            @RequestParam String phone,
                            @RequestParam Role role,
                            Model model) {

        String error = validateRegistration(fullName, email, password, confirmPassword);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("roles", Role.values());
            return "auth/register";
        }

        if (authService.emailExists(email)) {
            model.addAttribute("error", "An account with this email already exists.");
            model.addAttribute("roles", Role.values());
            return "auth/register";
        }

        authService.register(fullName, email, password, phone, role);
        return "redirect:/login?registered=true";
    }

    private String validateRegistration(String fullName, String email, String password, String confirmPassword) {
        if (fullName == null || fullName.isBlank()) return "Full name is required.";
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) return "Enter a valid email address.";
        if (password == null || password.length() < 6) return "Password must be at least 6 characters long.";
        if (confirmPassword != null && !password.equals(confirmPassword)) return "Passwords do not match.";
        return null;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                         @RequestParam String password,
                         HttpSession session,
                         Model model) {
        Optional<User> user = authService.login(email, password);
        if (user.isEmpty()) {
            model.addAttribute("error", "Invalid email or password.");
            return "auth/login";
        }
        session.setAttribute("loggedInUser", user.get());
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
