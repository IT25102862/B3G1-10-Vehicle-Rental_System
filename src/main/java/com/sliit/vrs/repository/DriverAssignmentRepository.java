package com.sliit.vrs.repository;

import com.sliit.vrs.entity.DriverAssignment;
import com.sliit.vrs.entity.Employee;
import com.sliit.vrs.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Spring Data JPA gives us save(), findAll(), findById(), deleteById() etc.
// for free - no SQL needs to be written for basic CRUD.
public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, Long> {

    // Used to stop a driver being double-booked, and a booking getting two drivers.
    boolean existsByDriverAndStatusIn(Employee driver, List<DriverAssignment.AssignmentStatus> statuses);

    boolean existsByReservationAndStatusIn(Reservation reservation, List<DriverAssignment.AssignmentStatus> statuses);
}
