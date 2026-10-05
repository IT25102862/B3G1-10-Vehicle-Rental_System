package com.sliit.vrs.service;

import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.User;
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
// v2: added a real date-range availability check (isVehicleAvailable) so
// two customers can't book the same vehicle for overlapping dates, and a
// getBookingsForCustomer() helper for the customer's "My Bookings" page.
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

    public List<Reservation> getBookingsForCustomer(Long customerId) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getCustomer() != null && r.getCustomer().getUserId().equals(customerId))
                .sorted((a, b) -> b.getReservationId().compareTo(a.getReservationId()))
                .toList();
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
    }

    /**
     * True if the vehicle has no CONFIRMED or PENDING_APPROVAL booking that
     * overlaps the requested date range. Two ranges overlap unless one ends
     * before the other starts.
     */
    public boolean isVehicleAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getVehicle().getVehicleId().equals(vehicleId))
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CONFIRMED
                        || r.getStatus() == Reservation.ReservationStatus.PENDING_APPROVAL)
                .noneMatch(r -> startDate.isBefore(r.getEndDate()) && endDate.isAfter(r.getStartDate()));
    }

    // CREATE a new booking. Calculates total amount from category base rate,
    // and blocks the booking if the vehicle isn't free for those dates.
    public Reservation createReservation(Reservation reservation, User customer) {
        Vehicle vehicle = reservation.getVehicle();

        if (vehicle.getAvailabilityStatus() == Vehicle.AvailabilityStatus.UNDER_MAINTENANCE) {
            throw new IllegalStateException("This vehicle is currently under maintenance.");
        }

        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
        if (days <= 0) {
            throw new IllegalArgumentException("Return date must be after the pickup date.");
        }

        if (!isVehicleAvailable(vehicle.getVehicleId(), reservation.getStartDate(), reservation.getEndDate())) {
            throw new IllegalStateException("This vehicle is already booked for part of the selected dates. Please choose different dates.");
        }

        double rate = vehicle.getCategory() != null ? vehicle.getCategory().getBaseRate() : 0;
        reservation.setCustomer(customer);
        reservation.setTotalAmount(days * rate);
        reservation.setReservationDate(LocalDate.now());
        reservation.setStatus(Reservation.ReservationStatus.PENDING_APPROVAL);

        return reservationRepository.save(reservation);
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


    // UPDATE - edit an existing booking's own fields (not just status).
    // Staff use this to correct a customer's dates/location after creation.
    public Reservation updateReservation(Long id, Reservation formData) {
        Reservation existing = getReservationById(id);
        existing.setStartDate(formData.getStartDate());
        existing.setEndDate(formData.getEndDate());
        existing.setPickupTime(formData.getPickupTime());
        existing.setReturnTime(formData.getReturnTime());
        existing.setPickupLocation(formData.getPickupLocation());
        existing.setDropoffLocation(formData.getDropoffLocation());
        existing.setNotes(formData.getNotes());

        // Recalculate the total in case the dates changed
        long days = ChronoUnit.DAYS.between(existing.getStartDate(), existing.getEndDate());
        double rate = existing.getVehicle().getCategory() != null ? existing.getVehicle().getCategory().getBaseRate() : 0;
        existing.setTotalAmount(days * rate);

        return reservationRepository.save(existing);
    }

    // DELETE - permanently remove a reservation record (e.g. a duplicate
    // or a mistaken entry). Different from cancelReservation(), which only
    // changes the status and keeps the record for history.
    public void deleteReservation(Long id) {
        Reservation r = getReservationById(id);
        // Free up the vehicle if this reservation had it locked.
        if (r.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
            vehicleService.markAsAvailable(r.getVehicle().getVehicleId());
        }
        reservationRepository.delete(r);
    }

    public void cancelReservation(Long id) {
        updateStatus(id, Reservation.ReservationStatus.CANCELLED);
    }
}
