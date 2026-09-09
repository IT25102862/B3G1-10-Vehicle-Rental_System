package com.sliit.vrs.service;

import com.sliit.vrs.entity.Role;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

// Shared / minor function: User Registration & Authentication.
// Built once in Phase 1 and reused by every module (login required
// before booking, viewing fleet, reporting emergencies, etc.)
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String fullName, String email, String rawPassword, String phone, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhoneNumber(phone);
        user.setRole(role);
        return userRepository.save(user);
    }

    // Returns the user if email + password match, otherwise empty.
    public Optional<User> login(String email, String rawPassword) {
        Optional<User> found = userRepository.findByEmail(email);
        if (found.isPresent() && passwordEncoder.matches(rawPassword, found.get().getPassword())) {
            return found;
        }
        return Optional.empty();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
