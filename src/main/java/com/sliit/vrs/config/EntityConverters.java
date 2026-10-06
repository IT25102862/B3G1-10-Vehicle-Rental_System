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

        // 1. VehicleCategory Converter
        registry.addConverter(new Converter<String, VehicleCategory>() {
            @Override
            public VehicleCategory convert(String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return categoryRepository.findById(Long.valueOf(source)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // 2. Vehicle Converter
        registry.addConverter(new Converter<String, Vehicle>() {
            @Override
            public Vehicle convert(String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return vehicleRepository.findById(Long.valueOf(source)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // 3. User Converter
        registry.addConverter(new Converter<String, User>() {
            @Override
            public User convert(String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return userRepository.findById(Long.valueOf(source)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // 4. Employee Converter
        registry.addConverter(new Converter<String, Employee>() {
            @Override
            public Employee convert(String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return employeeRepository.findById(Long.valueOf(source)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // 5. Reservation Converter
        registry.addConverter(new Converter<String, Reservation>() {
            @Override
            public Reservation convert(String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return reservationRepository.findById(Long.valueOf(source)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // 6. VehicleReturn Converter
        registry.addConverter(new Converter<String, VehicleReturn>() {
            @Override
            public VehicleReturn convert(String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return vehicleReturnRepository.findById(Long.valueOf(source)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // 7. DamageAssessment Converter
        registry.addConverter(new Converter<String, DamageAssessment>() {
            @Override
            public DamageAssessment convert(String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return damageAssessmentRepository.findById(Long.valueOf(source)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        // 8. TripPlan Converter
        registry.addConverter(new Converter<String, TripPlan>() {
            @Override
            public TripPlan convert(String source) {
                if (source == null || source.isBlank()) return null;
                try {
                    return tripPlanRepository.findById(Long.valueOf(source)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
    }
}