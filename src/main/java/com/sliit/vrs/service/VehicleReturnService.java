package com.sliit.vrs.service;

import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.VehicleReturn;
import com.sliit.vrs.repository.VehicleReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// ===================================================================
// MEMBER 4 (IT25101875 - Bathigama P.L.) - Vehicle Handover, Return &
// Damage Assessment
// ===================================================================
@Service
public class VehicleReturnService {

    @Autowired
    private VehicleReturnRepository returnRepository;

    @Autowired
    private ReservationService reservationService;

    public List<VehicleReturn> getAllReturns() {
        return returnRepository.findAll();
    }

    public VehicleReturn getById(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Return record not found with id: " + id));
    }

    // Record the vehicle return, then free up the vehicle and close the booking.
    public VehicleReturn recordReturn(VehicleReturn vehicleReturn) {
        VehicleReturn saved = returnRepository.save(vehicleReturn);
        reservationService.updateStatus(
                saved.getReservation().getReservationId(),
                Reservation.ReservationStatus.COMPLETED
        );
        return saved;
    }
}
