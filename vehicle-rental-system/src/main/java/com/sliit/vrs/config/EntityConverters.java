package com.sliit.vrs.config;

import com.sliit.vrs.entity.*;
import com.sliit.vrs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class EntityConverters implements WebMvcConfigurer {

    @Autowired private VehicleCategoryRepository categoryRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private VehicleReturnRepository vehicleReturnRepository;
    @Autowired private DamageAssessmentRepository damageAssessmentRepository;
    @Autowired private TripPlanRepository tripPlanRepository;

    @Override
    public void addFormatters(FormatterRegistry registry) {

        // VehicleCategory Converter
        registry.addConverter(new Converter<String, VehicleCategory>() {
            @Override
            public VehicleCategory convert(String id) {
                return (id == null || id.isBlank()) ? null : categoryRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });

        // Vehicle Converter
        registry.addConverter(new Converter<String, Vehicle>() {
            @Override
            public Vehicle convert(String id) {
                return (id == null || id.isBlank()) ? null : vehicleRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });

        // User Converter
        registry.addConverter(new Converter<String, User>() {
            @Override
            public User convert(String id) {
                return (id == null || id.isBlank()) ? null : userRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });

        // Employee Converter
        registry.addConverter(new Converter<String, Employee>() {
            @Override
            public Employee convert(String id) {
                return (id == null || id.isBlank()) ? null : employeeRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });

        // Reservation Converter
        registry.addConverter(new Converter<String, Reservation>() {
            @Override
            public Reservation convert(String id) {
                return (id == null || id.isBlank()) ? null : reservationRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });

        // VehicleReturn Converter
        registry.addConverter(new Converter<String, VehicleReturn>() {
            @Override
            public VehicleReturn convert(String id) {
                return (id == null || id.isBlank()) ? null : vehicleReturnRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });

        // DamageAssessment Converter
        registry.addConverter(new Converter<String, DamageAssessment>() {
            @Override
            public DamageAssessment convert(String id) {
                return (id == null || id.isBlank()) ? null : damageAssessmentRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });

        // TripPlan Converter
        registry.addConverter(new Converter<String, TripPlan>() {
            @Override
            public TripPlan convert(String id) {
                return (id == null || id.isBlank()) ? null : tripPlanRepository.findById(Long.valueOf(id)).orElse(null);
            }
        });
    }
}