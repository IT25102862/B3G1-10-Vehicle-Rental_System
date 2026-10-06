package com.sliit.vrs.repository;

import com.sliit.vrs.entity.Payment;
import com.sliit.vrs.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA gives us save(), findAll(), findById(), deleteById() etc.
// for free - no SQL needs to be written for basic CRUD.
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Used to stop the same booking from being paid twice.
    boolean existsByReservation(Reservation reservation);
}
