package com.sliit.vrs.controller;

import com.sliit.vrs.entity.User;
import com.sliit.vrs.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Customer Profile Management: view and edit personal details, and
// (optionally) upload a profile picture.
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        User current = (User) session.getAttribute("loggedInUser");
        // Re-fetch fresh from the DB in case it changed since login.
        User fresh = userService.getById(current.getUserId());
        model.addAttribute("user", fresh);
        return "customer/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User formUser,
                                 @RequestParam(required = false) MultipartFile profileImage,
                                 HttpSession session,
                                 Model model) {
        // Always update the LOGGED-IN user's own profile. The hidden userId in
        // the form could be changed by the user to edit someone else's account.
        User current = (User) session.getAttribute("loggedInUser");
        try {
            User updated = userService.updateProfile(current.getUserId(), formUser, profileImage);
            // Refresh the session copy so the navbar/dashboard show the new name immediately.
            session.setAttribute("loggedInUser", updated);
        } catch (RuntimeException ex) {
            // Show the error and keep what the user typed (email comes from the DB).
            User fresh = userService.getById(current.getUserId());
            formUser.setUserId(fresh.getUserId());
            formUser.setEmail(fresh.getEmail());
            formUser.setProfileImageUrl(fresh.getProfileImageUrl());
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("user", formUser);
            return "customer/profile";
        }
        return "redirect:/profile?updated=true";
    }
}
