package com.sliit.vrs.service;

import com.sliit.vrs.entity.User;
import com.sliit.vrs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Shared helper service for customer profile management and the admin's
// "Customer Management" screen.
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<User> getAllCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.sliit.vrs.entity.Role.CUSTOMER)
                .toList();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Updates only the editable profile fields (never overwrite the
    // password or role through this endpoint - that keeps the profile
    // form safe from accidentally granting someone admin rights).
    public User updateProfile(Long userId, User formData, MultipartFile profileImage) {
        User existing = getById(userId);
        existing.setFullName(formData.getFullName());
        existing.setPhoneNumber(formData.getPhoneNumber());
        existing.setAddress(formData.getAddress());
        existing.setDrivingLicenceNo(formData.getDrivingLicenceNo());

        if (profileImage != null && !profileImage.isEmpty()) {
            String url = fileStorageService.store(profileImage, "profiles");
            existing.setProfileImageUrl(url);
        }
        return userRepository.save(existing);
    }
}
