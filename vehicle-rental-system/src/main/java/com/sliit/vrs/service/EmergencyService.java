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

    public EmergencyRequest createRequest(EmergencyRequest request) {
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(EmergencyRequest.RequestStatus.OPEN);
        return emergencyRepository.save(request);
    }

    public void assignTechnician(Long requestId, com.sliit.vrs.entity.Employee technician) {
        EmergencyRequest request = getById(requestId);
        request.setAssignedTechnician(technician);
        request.setStatus(EmergencyRequest.RequestStatus.TECHNICIAN_ASSIGNED);
        emergencyRepository.save(request);
    }

    public void updateStatus(Long requestId, EmergencyRequest.RequestStatus status, String resolution) {
        EmergencyRequest request = getById(requestId);
        request.setStatus(status);
        if (resolution != null) {
            request.setResolution(resolution);
        }
        emergencyRepository.save(request);
    }

    public void cancelRequest(Long requestId) {
        EmergencyRequest request = getById(requestId);
        request.setStatus(EmergencyRequest.RequestStatus.CANCELLED);
        emergencyRepository.save(request);
    }
}
