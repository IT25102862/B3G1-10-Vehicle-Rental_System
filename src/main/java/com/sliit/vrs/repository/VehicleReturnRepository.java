package com.sliit.vrs.repository;

import com.sliit.vrs.entity.VehicleReturn;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA gives us save(), findAll(), findById(), deleteById() etc.
// for free - no SQL needs to be written for basic CRUD.
public interface VehicleReturnRepository extends JpaRepository<VehicleReturn, Long> {
}
