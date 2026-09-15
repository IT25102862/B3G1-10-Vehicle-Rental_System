package com.sliit.vrs.repository;

import com.sliit.vrs.entity.VehicleRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA gives us save(), findAll(), findById(), deleteById() etc.
// for free - no SQL needs to be written for basic CRUD.
public interface VehicleRecommendationRepository extends JpaRepository<VehicleRecommendation, Long> {
}
