package com.sliit.vrs.repository;

import com.sliit.vrs.entity.VehicleReturn;
import com.sliit.vrs.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA gives us save(), findAll(), findById(), deleteById() etc.
// for free - no SQL needs to be written for basic CRUD.
public interface VehicleReturnRepository extends JpaRepository<VehicleReturn, Long> {

    // A reservation can only have one return record (one-to-one link).
    boolean existsByReservation(Reservation reservation);
}
