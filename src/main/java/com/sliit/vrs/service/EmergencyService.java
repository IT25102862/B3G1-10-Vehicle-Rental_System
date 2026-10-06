package com.sliit.vrs.service;

import com.sliit.vrs.entity.EmergencyRequest;
import com.sliit.vrs.repository.EmergencyRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// ===================================================================
// MEMBER 3 (IT25103726 - Kithushan M.) - Emergency & Roadside Assistance
// ===================================================================
@Service
public class EmergencyService {

    @Autowired
    private EmergencyRequestRepository emergencyRepository;

    public List<EmergencyRequest> getAllRequests() {
        return emergencyRepository.findAll();
    }

    public EmergencyRequest getById(Long id) {
        return emergencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emergency request not found with id: " + id));
    }

    private static final int MAX_TEXT_LENGTH = 255;   // default VARCHAR size of these columns

    // Server-side checks for the "Report an Emergency" form.
    public void validateRequest(EmergencyRequest request) {
        if (request.getEmergencyType() == null) {
            throw new IllegalArgumentException("Please select the type of emergency.");
        }
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            throw new IllegalArgumentException("Location is required so our team can find you.");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Please describe what happened.");
        }
        request.setLocation(request.getLocation().trim());
        request.setDescription(request.getDescription().trim());
        if (request.getLocation().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Location is too long (maximum " + MAX_TEXT_LENGTH + " characters).");
        }
        if (request.getDescription().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Description is too long (maximum " + MAX_TEXT_LENGTH + " characters).");
        }
    }

    public EmergencyRequest createRequest(EmergencyRequest request) {
        if (request.getCustomer() == null) {
            throw new IllegalArgumentException("You must be logged in to report an emergency.");
        }
        validateRequest(request);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(EmergencyRequest.RequestStatus.OPEN);
        return emergencyRepository.save(request);
    }

    // RESOLVED and CANCELLED are final - a finished request cannot be
    // re-opened, re-assigned or cancelled again.
    private void checkNotClosed(EmergencyRequest request) {
        if (request.getStatus() == EmergencyRequest.RequestStatus.RESOLVED
                || request.getStatus() == EmergencyRequest.RequestStatus.CANCELLED) {
            throw new IllegalStateException("Emergency request #" + request.getEmergencyRequestId()
                    + " is already " + request.getStatus() + " and cannot be changed.");
        }
    }

    public void assignTechnician(Long requestId, com.sliit.vrs.entity.Employee technician) {
        EmergencyRequest request = getById(requestId);
        checkNotClosed(request);
        if (technician == null) {
            throw new IllegalArgumentException("Please select a valid technician.");
        }
        request.setAssignedTechnician(technician);
        request.setStatus(EmergencyRequest.RequestStatus.TECHNICIAN_ASSIGNED);
        emergencyRepository.save(request);
    }

    public void updateStatus(Long requestId, EmergencyRequest.RequestStatus status, String resolution) {
        EmergencyRequest request = getById(requestId);
        checkNotClosed(request);
        if (status == null || status == EmergencyRequest.RequestStatus.OPEN) {
            throw new IllegalArgumentException("Invalid status change.");
        }
        if (status == EmergencyRequest.RequestStatus.RESOLVED && (resolution == null || resolution.isBlank())) {
            throw new IllegalArgumentException("Please enter how the emergency was resolved.");
        }
        if (resolution != null && resolution.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Resolution note is too long (maximum " + MAX_TEXT_LENGTH + " characters).");
        }
        request.setStatus(status);
        if (resolution != null) {
            request.setResolution(resolution.trim());
        }
        emergencyRepository.save(request);
    }

    public void cancelRequest(Long requestId) {
        EmergencyRequest request = getById(requestId);
        checkNotClosed(request);
        request.setStatus(EmergencyRequest.RequestStatus.CANCELLED);
        emergencyRepository.save(request);
    }
}
