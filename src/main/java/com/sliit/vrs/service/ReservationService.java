package com.sliit.vrs.service;

import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.User;
import com.sliit.vrs.entity.Vehicle;
import com.sliit.vrs.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        if (vehicleId == null || startDate == null || endDate == null) {
            return false;
        }
        return reservationRepository.findAll().stream()
                .filter(r -> r.getVehicle().getVehicleId().equals(vehicleId))
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CONFIRMED
                        || r.getStatus() == Reservation.ReservationStatus.PENDING_APPROVAL)
                .noneMatch(r -> startDate.isBefore(r.getEndDate()) && endDate.isAfter(r.getStartDate()));
    }

    // ----- Booking validation rules (used by both the staff form and the
    // customer checkout flow, so the same rules apply everywhere) -----
    public static final int MAX_RENTAL_DAYS = 90;           // longest single booking allowed
    public static final int MAX_DAYS_IN_ADVANCE = 365;      // how far ahead a booking can start
    private static final int MAX_LOCATION_LENGTH = 255;     // matches the DB column size
    private static final int MAX_NOTES_LENGTH = 500;        // matches @Column(length = 500)

    /**
     * Checks every booking input and throws IllegalArgumentException with a
     * user-friendly message if something is wrong. Called on the server so the
     * rules cannot be skipped by editing the HTML form or sending a raw request.
     */
    public void validateBookingDetails(Reservation reservation) {
        if (reservation.getVehicle() == null) {
            throw new IllegalArgumentException("Please select a valid vehicle.");
        }
        if (reservation.getStartDate() == null || reservation.getEndDate() == null) {
            throw new IllegalArgumentException("Pickup date and return date are required.");
        }

        LocalDate today = LocalDate.now();
        if (reservation.getStartDate().isBefore(today)) {
            throw new IllegalArgumentException("Pickup date cannot be before today.");
        }
        if (reservation.getStartDate().isAfter(today.plusDays(MAX_DAYS_IN_ADVANCE))) {
            throw new IllegalArgumentException("Bookings can only be made up to " + MAX_DAYS_IN_ADVANCE + " days in advance.");
        }
        if (!reservation.getEndDate().isAfter(reservation.getStartDate())) {
            throw new IllegalArgumentException("Return date must be after the pickup date.");
        }
        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
        if (days > MAX_RENTAL_DAYS) {
            throw new IllegalArgumentException("A single booking cannot be longer than " + MAX_RENTAL_DAYS + " days.");
        }

        // A pickup later today must not be at a time that has already passed.
        if (reservation.getStartDate().isEqual(today) && reservation.getPickupTime() != null
                && LocalDateTime.of(today, reservation.getPickupTime()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Pickup time for today has already passed. Please choose a later time.");
        }

        if (reservation.getPickupLocation() == null || reservation.getPickupLocation().isBlank()) {
            throw new IllegalArgumentException("Pickup location is required.");
        }
        reservation.setPickupLocation(reservation.getPickupLocation().trim());
        if (reservation.getPickupLocation().length() > MAX_LOCATION_LENGTH) {
            throw new IllegalArgumentException("Pickup location is too long (maximum " + MAX_LOCATION_LENGTH + " characters).");
        }
        if (reservation.getDropoffLocation() != null && reservation.getDropoffLocation().trim().length() > MAX_LOCATION_LENGTH) {
            throw new IllegalArgumentException("Drop-off location is too long (maximum " + MAX_LOCATION_LENGTH + " characters).");
        }
        if (reservation.getNotes() != null && reservation.getNotes().length() > MAX_NOTES_LENGTH) {
            throw new IllegalArgumentException("Special requests are too long (maximum " + MAX_NOTES_LENGTH + " characters).");
        }
    }

    // CREATE a new booking. Calculates total amount from category base rate,
    // and blocks the booking if the vehicle isn't free for those dates.
    public Reservation createReservation(Reservation reservation, User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("You must be logged in to make a booking.");
        }
        validateBookingDetails(reservation);

        Vehicle vehicle = reservation.getVehicle();

        if (vehicle.getAvailabilityStatus() == Vehicle.AvailabilityStatus.UNDER_MAINTENANCE) {
            throw new IllegalStateException("This vehicle is currently under maintenance.");
        }

        // A vehicle without a valid daily rate would produce a Rs. 0 booking.
        if (vehicle.getCategory() == null || vehicle.getCategory().getBaseRate() == null
                || vehicle.getCategory().getBaseRate() <= 0) {
            throw new IllegalStateException("This vehicle does not have a valid daily rate yet and cannot be booked.");
        }

        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());

        if (!isVehicleAvailable(vehicle.getVehicleId(), reservation.getStartDate(), reservation.getEndDate())) {
            throw new IllegalStateException("This vehicle is already booked for part of the selected dates. Please choose different dates.");
        }

        double rate = vehicle.getCategory().getBaseRate();
        reservation.setCustomer(customer);
        reservation.setTotalAmount(days * rate);
        reservation.setReservationDate(LocalDate.now());
        reservation.setStatus(Reservation.ReservationStatus.PENDING_APPROVAL);

        return reservationRepository.save(reservation);
    }

    // Allowed status changes:
    //   PENDING_APPROVAL -> CONFIRMED or CANCELLED
    //   CONFIRMED        -> COMPLETED or CANCELLED
    //   CANCELLED / COMPLETED are final and cannot be changed again.
    // Without this check, someone could e.g. "confirm" a cancelled booking by
    // typing the URL directly, which would wrongly mark the vehicle as rented.
    public boolean isValidStatusChange(Reservation.ReservationStatus from, Reservation.ReservationStatus to) {
        if (from == null || to == null) return false;
        switch (from) {
            case PENDING_APPROVAL:
                return to == Reservation.ReservationStatus.CONFIRMED || to == Reservation.ReservationStatus.CANCELLED;
            case CONFIRMED:
                return to == Reservation.ReservationStatus.COMPLETED || to == Reservation.ReservationStatus.CANCELLED;
            default:
                return false;
        }
    }

    public void updateStatus(Long id, Reservation.ReservationStatus status) {
        Reservation r = getReservationById(id);
        if (!isValidStatusChange(r.getStatus(), status)) {
            throw new IllegalStateException("Booking #" + id + " is " + r.getStatus()
                    + " and cannot be changed to " + status + ".");
        }
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
