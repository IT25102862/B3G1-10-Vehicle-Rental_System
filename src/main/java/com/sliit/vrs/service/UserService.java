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
    // ----- Shared input rules (also used by AuthController for registration) -----

    // Letters (any language), spaces, dots, apostrophes and hyphens, e.g. "Panapitiya P.K.S.C."
    public static boolean isValidFullName(String name) {
        return name != null && name.trim().matches("[\\p{L} .'-]{2,100}");
    }

    // Sri Lankan numbers: 0771234567 or +94771234567 (spaces/dashes are ignored).
    public static boolean isValidPhone(String phone) {
        return phone != null && normalizePhone(phone).matches("(0\\d{9}|\\+94\\d{9})");
    }

    public static String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("[\\s-]", "");
    }

    public void validateProfile(User formData) {
        if (!isValidFullName(formData.getFullName())) {
            throw new IllegalArgumentException("Full name must be 2-100 characters and contain only letters, spaces, dots, apostrophes or hyphens.");
        }
        if (formData.getPhoneNumber() != null && !formData.getPhoneNumber().isBlank()
                && !isValidPhone(formData.getPhoneNumber())) {
            throw new IllegalArgumentException("Enter a valid phone number, e.g. 0771234567 or +94771234567.");
        }
        if (formData.getAddress() != null && formData.getAddress().length() > 255) {
            throw new IllegalArgumentException("Address is too long (maximum 255 characters).");
        }
        if (formData.getDrivingLicenceNo() != null && !formData.getDrivingLicenceNo().isBlank()
                && !formData.getDrivingLicenceNo().replaceAll("\\s", "").matches("[A-Za-z0-9]{5,15}")) {
            throw new IllegalArgumentException("Driving licence number must be 5-15 letters or numbers, e.g. B1234567.");
        }
    }

    public User updateProfile(Long userId, User formData, MultipartFile profileImage) {
        User existing = getById(userId);
        validateProfile(formData);
        existing.setFullName(formData.getFullName().trim());
        String phone = formData.getPhoneNumber();
        existing.setPhoneNumber(phone == null || phone.isBlank() ? null : normalizePhone(phone));
        existing.setAddress(formData.getAddress() == null ? null : formData.getAddress().trim());
        String licence = formData.getDrivingLicenceNo();
        existing.setDrivingLicenceNo(licence == null || licence.isBlank()
                ? null : licence.replaceAll("\\s", "").toUpperCase());

        if (profileImage != null && !profileImage.isEmpty()) {
            String url = fileStorageService.store(profileImage, "profiles");
            existing.setProfileImageUrl(url);
        }
        return userRepository.save(existing);
    }
}
