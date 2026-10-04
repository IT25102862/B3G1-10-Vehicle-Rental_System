package com.sliit.vrs.service;

import com.sliit.vrs.entity.VehicleCategory;
import com.sliit.vrs.repository.VehicleCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// MEMBER 1 - Vehicle Fleet Management
@Service
public class VehicleCategoryService {

    @Autowired
    private VehicleCategoryRepository categoryRepository;

    public List<VehicleCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public VehicleCategory saveCategory(VehicleCategory category) {
        return categoryRepository.save(category);
    }
}
