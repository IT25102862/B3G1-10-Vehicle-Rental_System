package com.sliit.vrs.service;

import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.VehicleReturn;
import com.sliit.vrs.repository.VehicleReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public static final List<String> FUEL_LEVELS = List.of("Full", "3/4", "Half", "1/4", "Empty");
    private static final double MAX_MILEAGE = 2_000_000;
    private static final int MAX_TEXT_LENGTH = 255;

    // Server-side checks for the "Record Vehicle Return" form.
    public void validateReturn(VehicleReturn vehicleReturn) {
        Reservation reservation = vehicleReturn.getReservation();
        if (reservation == null) {
            throw new IllegalArgumentException("Please select a valid reservation.");
        }
        // Only a CONFIRMED booking has a vehicle out with the customer.
        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Booking #" + reservation.getReservationId() + " is "
                    + reservation.getStatus() + ". Only CONFIRMED bookings can be returned.");
        }
        if (returnRepository.existsByReservation(reservation)) {
            throw new IllegalStateException("A return has already been recorded for booking #" + reservation.getReservationId() + ".");
        }

        LocalDate today = LocalDate.now();
        if (vehicleReturn.getReturnDate() == null) {
            throw new IllegalArgumentException("Return date is required.");
        }
        // A return can be entered late (e.g. yesterday's return typed in today),
        // but it cannot be before the rental started or in the future.
        if (reservation.getStartDate() != null && vehicleReturn.getReturnDate().isBefore(reservation.getStartDate())) {
            throw new IllegalArgumentException("Return date cannot be before the pickup date (" + reservation.getStartDate() + ").");
        }
        if (vehicleReturn.getReturnDate().isAfter(today)) {
            throw new IllegalArgumentException("Return date cannot be in the future.");
        }
        if (vehicleReturn.getReturnDate().isEqual(today) && vehicleReturn.getReturnTime() != null
                && LocalDateTime.of(today, vehicleReturn.getReturnTime()).isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Return time cannot be in the future.");
        }

        Double mileage = vehicleReturn.getReturnMileage();
        if (mileage != null) {
            if (mileage < 0 || mileage > MAX_MILEAGE) {
                throw new IllegalArgumentException("Return mileage must be between 0 and " + (long) MAX_MILEAGE + " km.");
            }
            // The odometer cannot go backwards.
            Double startMileage = reservation.getVehicle() != null ? reservation.getVehicle().getMileage() : null;
            if (startMileage != null && mileage < startMileage) {
                throw new IllegalArgumentException("Return mileage (" + mileage + " km) cannot be less than the vehicle's recorded mileage ("
                        + startMileage + " km).");
            }
        }

        if (vehicleReturn.getFuelLevel() != null && !vehicleReturn.getFuelLevel().isBlank()
                && !FUEL_LEVELS.contains(vehicleReturn.getFuelLevel())) {
            throw new IllegalArgumentException("Please choose a valid fuel level.");
        }
        if (vehicleReturn.getReturnCondition() != null && vehicleReturn.getReturnCondition().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Return condition notes are too long (maximum " + MAX_TEXT_LENGTH + " characters).");
        }
    }

    // Record the vehicle return, then free up the vehicle and close the booking.
    // @Transactional: if closing the booking fails, the return is not saved either.
    @Transactional
    public VehicleReturn recordReturn(VehicleReturn vehicleReturn) {
        validateReturn(vehicleReturn);
        VehicleReturn saved = returnRepository.save(vehicleReturn);
        reservationService.updateStatus(
                saved.getReservation().getReservationId(),
                Reservation.ReservationStatus.COMPLETED
        );
        return saved;
    }
}
