package com.sliit.vrs.service;

import com.sliit.vrs.entity.Damage;
import com.sliit.vrs.entity.DamageAssessment;
import com.sliit.vrs.entity.VehicleReturn;
import com.sliit.vrs.repository.DamageAssessmentRepository;
import com.sliit.vrs.repository.DamageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// MEMBER 4 - Vehicle Handover, Return & Damage Assessment
@Service
public class DamageAssessmentService {

    @Autowired
    private DamageAssessmentRepository assessmentRepository;

    @Autowired
    private DamageRepository damageRepository;

    public List<DamageAssessment> getAllAssessments() {
        return assessmentRepository.findAll();
    }

    public static final List<String> CONDITIONS = List.of("Good", "Fair", "Poor");
    public static final List<String> SEVERITIES = List.of("Minor", "Moderate", "Severe");
    private static final double MAX_REPAIR_COST = 10_000_000;   // Rs. - sanity limit
    private static final int MAX_TEXT_LENGTH = 255;

    public DamageAssessment saveAssessment(DamageAssessment assessment) {
        VehicleReturn vehicleReturn = assessment.getVehicleReturn();
        if (vehicleReturn == null) {
            throw new IllegalArgumentException("Return record not found for this assessment.");
        }
        // One assessment per return (one-to-one link in the database).
        if (assessment.getAssessmentId() == null && assessmentRepository.existsByVehicleReturn(vehicleReturn)) {
            throw new IllegalStateException("A damage assessment has already been recorded for return #"
                    + vehicleReturn.getReturnId() + ".");
        }
        if (assessment.getAssessmentDate() == null) {
            throw new IllegalArgumentException("Assessment date is required.");
        }
        if (vehicleReturn.getReturnDate() != null && assessment.getAssessmentDate().isBefore(vehicleReturn.getReturnDate())) {
            throw new IllegalArgumentException("Assessment date cannot be before the return date (" + vehicleReturn.getReturnDate() + ").");
        }
        if (assessment.getAssessmentDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Assessment date cannot be in the future.");
        }
        if (assessment.getOverallCondition() == null || !CONDITIONS.contains(assessment.getOverallCondition())) {
            throw new IllegalArgumentException("Please choose the overall condition (Good, Fair or Poor).");
        }
        // Zero is allowed (no damage), negative is not.
        checkCost(assessment.getEstimatedRepairCost(), "Estimated repair cost");
        if (assessment.getAssessorRemark() != null && assessment.getAssessorRemark().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Assessor remark is too long (maximum " + MAX_TEXT_LENGTH + " characters).");
        }
        return assessmentRepository.save(assessment);
    }

    public Damage addDamage(Damage damage) {
        if (damage.getAssessment() == null) {
            throw new IllegalArgumentException("Damage assessment not found.");
        }
        if (damage.getDamageType() == null || damage.getDamageType().isBlank()) {
            throw new IllegalArgumentException("Damage type is required.");
        }
        if (damage.getDamageType().length() > 100) {
            throw new IllegalArgumentException("Damage type is too long (maximum 100 characters).");
        }
        if (damage.getSeverity() == null || !SEVERITIES.contains(damage.getSeverity())) {
            throw new IllegalArgumentException("Severity must be Minor, Moderate or Severe.");
        }
        checkCost(damage.getRepairCost(), "Repair cost");
        damage.setDamageStatus(Damage.DamageStatus.REPORTED);
        return damageRepository.save(damage);
    }

    private void checkCost(Double cost, String fieldName) {
        if (cost != null && (cost < 0 || cost > MAX_REPAIR_COST)) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and " + (long) MAX_REPAIR_COST + ".");
        }
    }

    public List<Damage> getAllDamages() {
        return damageRepository.findAll();
    }
}
