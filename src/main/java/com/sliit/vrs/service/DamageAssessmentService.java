package com.sliit.vrs.service;

import com.sliit.vrs.entity.Damage;
import com.sliit.vrs.entity.DamageAssessment;
import com.sliit.vrs.repository.DamageAssessmentRepository;
import com.sliit.vrs.repository.DamageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public DamageAssessment saveAssessment(DamageAssessment assessment) {
        return assessmentRepository.save(assessment);
    }

    public Damage addDamage(Damage damage) {
        damage.setDamageStatus(Damage.DamageStatus.REPORTED);
        return damageRepository.save(damage);
    }

    public List<Damage> getAllDamages() {
        return damageRepository.findAll();
    }
}
