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
                            @RequestParam String phone,
                            @RequestParam Role role,
                            Model model) {
        if (authService.emailExists(email)) {
            model.addAttribute("error", "An account with this email already exists.");
            model.addAttribute("roles", Role.values());
            return "auth/register";
        }
        authService.register(fullName, email, password, phone, role);
        return "redirect:/login?registered=true";
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
