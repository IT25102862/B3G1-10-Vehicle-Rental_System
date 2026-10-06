package com.sliit.vrs.service;

import com.sliit.vrs.entity.DriverAssignment;
import com.sliit.vrs.entity.Employee;
import com.sliit.vrs.entity.Reservation;
import com.sliit.vrs.entity.Role;
import com.sliit.vrs.repository.DriverAssignmentRepository;
import com.sliit.vrs.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// MEMBER 6 - Maintenance Operations Management System (Driver Allocation)
@Service
public class DriverAssignmentService {

    @Autowired
    private DriverAssignmentRepository assignmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<DriverAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    private static final List<DriverAssignment.AssignmentStatus> ACTIVE_STATUSES =
            List.of(DriverAssignment.AssignmentStatus.ASSIGNED, DriverAssignment.AssignmentStatus.ON_TRIP);

    // Server-side checks for the "Assign a Driver" form.
    public void validateAssignment(DriverAssignment assignment) {
        Reservation reservation = assignment.getReservation();
        Employee driver = assignment.getDriver();
        if (reservation == null) {
            throw new IllegalArgumentException("Please select a valid reservation.");
        }
        if (driver == null) {
            throw new IllegalArgumentException("Please select a valid driver.");
        }
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING_APPROVAL
                && reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Booking #" + reservation.getReservationId() + " is "
                    + reservation.getStatus() + " - a driver can only be assigned to an active booking.");
        }
        if (driver.getRole() != Role.DRIVER) {
            throw new IllegalArgumentException(driver.getName() + " is not a driver.");
        }
        if (driver.getDriverStatus() == Employee.DriverStatus.OFF_DUTY) {
            throw new IllegalStateException(driver.getName() + " is off duty and cannot be assigned.");
        }
        if (assignmentRepository.existsByDriverAndStatusIn(driver, ACTIVE_STATUSES)) {
            throw new IllegalStateException(driver.getName() + " is already assigned to another trip.");
        }
        if (assignmentRepository.existsByReservationAndStatusIn(reservation, ACTIVE_STATUSES)) {
            throw new IllegalStateException("Booking #" + reservation.getReservationId() + " already has a driver assigned.");
        }
    }

    public DriverAssignment assignDriver(DriverAssignment assignment) {
        assignment.setAssignmentId(null);   // this form always creates a new assignment
        validateAssignment(assignment);
        assignment.setAssignedDate(LocalDate.now());
        assignment.setStatus(DriverAssignment.AssignmentStatus.ASSIGNED);

        // Update the driver's own availability record too (not just the assignment row).
        Employee driver = assignment.getDriver();
        driver.setDriverStatus(Employee.DriverStatus.ON_SHIFT);
        employeeRepository.save(driver);

        return assignmentRepository.save(assignment);
    }

    // Allowed: ASSIGNED -> ON_TRIP / COMPLETED / CANCELLED, ON_TRIP -> COMPLETED / CANCELLED.
    // COMPLETED and CANCELLED are final.
    public void updateStatus(Long id, DriverAssignment.AssignmentStatus status) {
        DriverAssignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + id));
        if (!ACTIVE_STATUSES.contains(assignment.getStatus()) || status == null
                || status == DriverAssignment.AssignmentStatus.ASSIGNED || status == assignment.getStatus()) {
            throw new IllegalStateException("Assignment #" + id + " is " + assignment.getStatus()
                    + " and cannot be changed to " + status + ".");
        }
        assignment.setStatus(status);
        assignmentRepository.save(assignment);

        // When the trip ends, the driver becomes free again. (Previously the
        // driver stayed ON_SHIFT forever after their first assignment.)
        if (status == DriverAssignment.AssignmentStatus.COMPLETED
                || status == DriverAssignment.AssignmentStatus.CANCELLED) {
            Employee driver = assignment.getDriver();
            if (driver != null && !assignmentRepository.existsByDriverAndStatusIn(driver, ACTIVE_STATUSES)) {
                driver.setDriverStatus(Employee.DriverStatus.AVAILABLE);
                employeeRepository.save(driver);
            }
        }
    }
}
