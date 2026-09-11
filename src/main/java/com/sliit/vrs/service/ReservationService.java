package com.sliit.vrs.service;

import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

// ===================================================================
// MEMBER 2 (IT25102862 - Sandaruwan K.G.A.) - Booking & Reservation
// Management
// ===================================================================
@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private VehicleService vehicleService;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
    }

    // CREATE a new booking. Calculates total amount from category base rate
    // and blocks the vehicle from being booked twice (simple overlap guard).
    public Reservation createReservation(Reservation reservation) {
        Vehicle vehicle = reservation.getVehicle();

        if (vehicle.getAvailabilityStatus() != Vehicle.AvailabilityStatus.AVAILABLE) {
            throw new IllegalStateException("Selected vehicle is not available for booking.");
        }

        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
        if (days <= 0) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        double rate = vehicle.getCategory() != null ? vehicle.getCategory().getBaseRate() : 0;
        reservation.setTotalAmount(days * rate);
        reservation.setReservationDate(LocalDate.now());
        reservation.setStatus(Reservation.ReservationStatus.PENDING_APPROVAL);

        Reservation saved = reservationRepository.save(reservation);

        // Prevent double-booking: mark vehicle as rented once confirmed by staff.
        return saved;
    }

    public void updateStatus(Long id, Reservation.ReservationStatus status) {
        Reservation r = getReservationById(id);
        r.setStatus(status);
        reservationRepository.save(r);

        if (status == Reservation.ReservationStatus.CONFIRMED) {
            vehicleService.markAsRented(r.getVehicle().getVehicleId());
        }
        if (status == Reservation.ReservationStatus.CANCELLED || status == Reservation.ReservationStatus.COMPLETED) {
            vehicleService.markAsAvailable(r.getVehicle().getVehicleId());
        }
    }

    public void cancelReservation(Long id) {
        updateStatus(id, Reservation.ReservationStatus.CANCELLED);
    }
}
